package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.api.remapping.InsnConflictRemapProvider
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.ServiceLoader

object WorkaroundFixer {
    private val insnConflictRemapProviders = ServiceLoader.load(InsnConflictRemapProvider::class.java)

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

                                if (remapped != insnNode.name) {
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

                    insnNode.bsmArgs = newArgs;
                }

                if (insnNode is FieldInsnNode && insnNode.owner == minecraftMapped) {
                    // We need to remap Forge mods to use the GUI that they're expecting
                    if (insnNode.name == "f_91065_" || insnNode.name == mcGuiMapped || insnNode.name == "gui") {
                        val followingInsn = method.instructions.get(method.instructions.indexOf(insnNode) + 1)

                        if (followingInsn.opcode == Opcodes.CHECKCAST && followingInsn is TypeInsnNode && followingInsn.desc == "net/minecraftforge/client/gui/overlay/ForgeGui") {
                            newNodeMap[insnNode] = FieldInsnNode(Opcodes.GETFIELD, insnNode.owner, "kilt\$forgeGui", insnNode.desc)
                        }
                    }
                } else if (insnNode is MethodInsnNode && insnNode.owner == "java/lang/System" && insnNode.name == "exit" && insnNode.desc == "(I)V") {
                    // See: KiltHelper.handleSystemExit()
                    newNodeMap[insnNode] = MethodInsnNode(Opcodes.INVOKESTATIC, "xyz/bluspring/kilt/util/KiltHelper", "handleSystemExit", insnNode.desc)
                } else if (insnNode is TypeInsnNode && insnNode.opcode == Opcodes.INSTANCEOF && insnNode.desc == "org/violetmoon/quark/mixin/mixins/client/accessor/AccessorCustomCreativeSlot") {
                    newNodeMap[insnNode] = TypeInsnNode(Opcodes.INSTANCEOF, customSlotMapped)
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