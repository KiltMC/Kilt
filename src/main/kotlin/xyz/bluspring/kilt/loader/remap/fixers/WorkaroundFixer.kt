package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object WorkaroundFixer {
    private val minecraftMapped = KiltRemapper.remapClass("net/minecraft/client/Minecraft")
    private val potionBrewingMapped = KiltRemapper.remapClass("net/minecraft/world/item/alchemy/PotionBrewing\$Mix")

    private val mappingResolver = FabricLoader.getInstance().mappingResolver
    private val pbFromMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1845\$class_1846", "field_8962", "Ljava/lang/Object;")
    private val pbToMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1845\$class_1846", "field_8961", "Ljava/lang/Object;")
    private val mcGuiMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_310", "field_1705", "Lnet/minecraft/class_329;")

    fun fixClass(classNode: ClassNode) {
        val methodReplace = mutableListOf<MethodNode>()

        for (method in classNode.methods) {
            val newNodeMap = mutableMapOf<AbstractInsnNode, AbstractInsnNode>()

            for (insnNode in method.instructions) {
                if (insnNode is MethodInsnNode && insnNode.owner == "net/minecraftforge/fluids/FluidStack") {
                    if (insnNode.name == "getAmount") {
                        val node = MethodInsnNode(insnNode.opcode, "net/minecraftforge/fluids/FluidStack", "forge\$getAmount", insnNode.desc)
                        newNodeMap[insnNode] = node
                    } else if (insnNode.name == "writeToPacket") {
                        val node = MethodInsnNode(insnNode.opcode, "net/minecraftforge/fluids/FluidStack", "forge\$writeToPacket", insnNode.desc)
                        newNodeMap[insnNode] = node
                    }
                } else if (insnNode is FieldInsnNode && insnNode.owner == potionBrewingMapped) {
                    if (insnNode.name == pbFromMapped || insnNode.name == "from") {
                        val node = FieldInsnNode(insnNode.opcode, insnNode.owner, "kilt\$from", insnNode.desc)
                        newNodeMap[insnNode] = node
                    } else if (insnNode.name == pbToMapped || insnNode.name == "to") {
                        val node = FieldInsnNode(insnNode.opcode, insnNode.owner, "kilt\$to", insnNode.desc)
                        newNodeMap[insnNode] = node
                    }
                } else if (insnNode is FieldInsnNode && insnNode.owner == minecraftMapped) {
                    if (insnNode.name == mcGuiMapped || insnNode.name == "gui") {
                        val followingInsn = method.instructions.get(method.instructions.indexOf(insnNode) + 1)

                        if (followingInsn.opcode == Opcodes.CHECKCAST && followingInsn is TypeInsnNode && followingInsn.desc == "net/minecraftforge/client/gui/overlay/ForgeGui") {
                            newNodeMap[insnNode] = FieldInsnNode(Opcodes.GETFIELD, insnNode.owner, "kilt\$forgeGui", insnNode.desc)
                        }
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