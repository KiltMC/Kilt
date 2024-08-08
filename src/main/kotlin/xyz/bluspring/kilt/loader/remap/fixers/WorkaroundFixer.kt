package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object WorkaroundFixer {
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