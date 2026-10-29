package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

// Wraps all calls relating to "layer.findModule().orElseThrow()"
object RemoveModulesFixer {
    fun fixClass(classNode: ClassNode) {
        for (methodNode in classNode.methods) {
            val afterInsns = mutableListOf<AbstractInsnNode>()

            for (insnNode in methodNode.instructions) {
                if (insnNode is MethodInsnNode && insnNode.opcode == Opcodes.INVOKEVIRTUAL && insnNode.owner == "java/lang/ModuleLayer" && insnNode.name == "findModule") {
                    afterInsns.add(insnNode)
                }
            }

            for (insnNode in afterInsns) {
                methodNode.instructions.insert(insnNode, MethodInsnNode(Opcodes.INVOKESTATIC, "xyz/bluspring/kilt/util/KiltHelper", "resolveModuleToUnnamed", "(Ljava/util/Optional;)Ljava/util/Optional;", false))
            }
        }
    }
}
