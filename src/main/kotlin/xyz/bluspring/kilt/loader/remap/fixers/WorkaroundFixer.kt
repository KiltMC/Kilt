package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.api.remapping.InsnConflictRemapProvider
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.*

object WorkaroundFixer {
    private val insnConflictRemapProviders = ServiceLoader.load(InsnConflictRemapProvider::class.java).toList()

    private val mappingResolver = FabricLoader.getInstance().mappingResolver

    private val customSlotMapped = KiltRemapper.remapClass("net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen\$CustomCreativeSlot")
    private val minecraftMapped = KiltRemapper.remapClass("net/minecraft/client/Minecraft")
    private val mcGuiMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_310", "field_1705", "Lnet/minecraft/class_329;")

    fun fixClass(classNode: ClassNode) {
        val methodReplace = mutableListOf<MethodNode>()

        for (method in classNode.methods) {
            val newNodeMap = mutableMapOf<AbstractInsnNode, AbstractInsnNode>()

            insnRemap@for (insnNode in method.instructions) {
                // Handle remaps through the additional remap providers first
                if (insnNode is MethodInsnNode) {
                    for (provider in insnConflictRemapProviders) {
                        val remapped = provider.remapMethod(insnNode.owner, insnNode.name, insnNode.desc)

                        if (remapped != insnNode.name) {
                            KiltRemapper.logger.debug("Provider ${provider::class.java.name} remapped method ${insnNode.owner}#${insnNode.name}${insnNode.desc} to $remapped")

                            val node = MethodInsnNode(insnNode.opcode, insnNode.owner, remapped, insnNode.desc)
                            newNodeMap[insnNode] = node

                            continue@insnRemap
                        }
                    }
                } else if (insnNode is FieldInsnNode) {
                    for (provider in insnConflictRemapProviders) {
                        val remapped = provider.remapField(insnNode.owner, insnNode.name, insnNode.desc)

                        if (remapped != insnNode.name) {
                            KiltRemapper.logger.debug("Provider ${provider::class.java.name} remapped field ${insnNode.owner}#${insnNode.name}${insnNode.desc} to $remapped")

                            val node = FieldInsnNode(insnNode.opcode, insnNode.owner, remapped, insnNode.desc)
                            newNodeMap[insnNode] = node

                            continue@insnRemap
                        }
                    }
                } else if (insnNode is InvokeDynamicInsnNode) {
                    val newArgs = arrayOfNulls<Any>(insnNode.bsmArgs.size)
                    for ((i, arg) in insnNode.bsmArgs.withIndex()) {
                        if (arg is Handle) {
                            var newName = arg.name

                            for (provider in insnConflictRemapProviders) {
                                val remapped = provider.remapMethod(arg.owner, arg.name, arg.desc)

                                if (remapped != arg.name) {
                                    KiltRemapper.logger.debug("Provider ${provider::class.java.name} remapped dynamic method ${arg.owner}#${arg.name}${arg.desc} to $remapped")
                                    newName = remapped

                                    break
                                }
                            }

                            if (newName != arg.name) {
                                newArgs[i] = Handle(arg.tag, arg.owner, newName, arg.desc, arg.isInterface)
                                continue
                            }
                        }

                        newArgs[i] = arg
                    }

                    insnNode.bsmArgs = newArgs
                }

                when (insnNode) {
                    is MethodInsnNode if insnNode.owner == "java/lang/System" && insnNode.name == "exit" && insnNode.desc == "(I)V" -> {
                        // See: KiltHelper.handleSystemExit()
                        newNodeMap[insnNode] = MethodInsnNode(Opcodes.INVOKESTATIC, "xyz/bluspring/kilt/util/KiltHelper", "handleSystemExit", insnNode.desc)
                    }

                    is TypeInsnNode if insnNode.opcode == Opcodes.INSTANCEOF && insnNode.desc == "org/violetmoon/quark/mixin/mixins/client/accessor/AccessorCustomCreativeSlot" -> {
                        newNodeMap[insnNode] = TypeInsnNode(Opcodes.INSTANCEOF, customSlotMapped)
                    }

                    is MethodInsnNode if insnNode.opcode == Opcodes.INVOKESPECIAL
                        && insnNode.owner == KiltRemapper.remapClass("net/minecraft/world/item/crafting/Ingredient") && insnNode.name == "<init>"
                        && insnNode.desc == "(Lnet/neoforged/neoforge/common/crafting/ICustomIngredient;)V" -> {
                        newNodeMap[insnNode] = MethodInsnNode(
                            insnNode.opcode,
                            "xyz/bluspring/kilt/workarounds/resource/KiltCustomIngredient",
                            insnNode.name,
                            insnNode.desc,
                            insnNode.itf
                        )
                    }
                }
            }

            if (newNodeMap.isNotEmpty()) {
                for ((oldNode, newNode) in newNodeMap) {
                    method.instructions.set(oldNode, newNode)
                }

                methodReplace.add(method)
            }
        }

        classNode.methods.removeIf { methodReplace.any { a -> it.name == a.name && a.desc == it.desc } }
        classNode.methods.addAll(methodReplace)
    }
}
