package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

// Porting Lib makes some methods virtual instead of static.
// Let's remap the methods that conflict with Porting Lib.
object ConflictingStaticMethodFixer {
    private val conflictingMethods = mapOf<String, List<Pair<String, String>>>()

    fun fixClass(classNode: ClassNode) {
        for (method in classNode.methods) {
            val newNodeMap = mutableMapOf<AbstractInsnNode, AbstractInsnNode>()

            for (insnNode in method.instructions) {
                // Target static invokes specifically
                if (insnNode is MethodInsnNode && insnNode.opcode == Opcodes.INVOKESTATIC) {
                    val specificClass = conflictingMethods.keys.firstOrNull { it == insnNode.owner } ?: continue
                    val methodList = conflictingMethods[specificClass]!!

                    if (methodList.none { it.first == insnNode.name && it.second == insnNode.desc })
                        continue

                    // prefix with Forge
                    val node = MethodInsnNode(insnNode.opcode, insnNode.owner, "forge\$${insnNode.name}", insnNode.desc)
                    newNodeMap[insnNode] = node
                }
            }

            if (newNodeMap.isNotEmpty()) {
                for ((oldNode, newNode) in newNodeMap) {
                    method.instructions.set(oldNode, newNode)
                }
            }
        }
    }
}
