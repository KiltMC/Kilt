package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode

// When you have to deal with reflection in a codebase, remap all of them!
// *this is going to be so bad for performance...*
object ReflectionFixer {
    const val HELPER_CLASS = "xyz/bluspring/kilt/util/KiltReflectionHelper"

    fun fixClass(classNode: ClassNode) {
        for (methodNode in classNode.methods) {
            var wasInitialized = false
            val nodesToReplace by lazy {
                wasInitialized = true
                mutableMapOf<AbstractInsnNode, AbstractInsnNode>()
            }

            for (node in methodNode.instructions) {
                if (node is MethodInsnNode) {
                    if (node.opcode == Opcodes.INVOKESTATIC && node.owner == "java/lang/Class" && node.name == "forName") { // Class.forName
                        nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "getClass", node.desc, false)
                    } else if (node.opcode == Opcodes.INVOKEVIRTUAL && node.owner == "java/lang/ClassLoader" && node.name == "loadClass") { // classLoader.loadClass
                        nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "loadClass", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", false)
                    } else if (node.opcode == Opcodes.INVOKEVIRTUAL && node.owner == "java/lang/Class") {
                        if (node.name == "getField" || node.name == "getDeclaredField") { // clazz.getField / clazz.getDeclaredField
                            nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, node.name, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false)
                        } else if (node.name == "getMethod" || node.name == "getDeclaredMethod") { // clazz.getMethod / clazz.getDeclaredMethod
                            nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, node.name, "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false)
                        }
                    } else if (node.opcode == Opcodes.INVOKEVIRTUAL && node.owner == "java/lang/reflect/Field" && node.name == "getName" && node.desc == "()Ljava/lang/String;") {
                        nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "getMojFieldName", "(Ljava/lang/reflect/Field;)Ljava/lang/String;", false)
                    } else if (node.opcode == Opcodes.INVOKEVIRTUAL && node.owner == "java/lang/reflect/Method" && node.name == "getName" && node.desc == "()Ljava/lang/String;") {
                        nodesToReplace[node] = MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "getMojMethodName", "(Ljava/lang/reflect/Method;)Ljava/lang/String;", false)
                    }
                }
            }

            if (wasInitialized) {
                val insns = InsnList()
                insns.add(methodNode.instructions)

                for ((fromInsn, toInsn) in nodesToReplace) {
                    insns.set(fromInsn, toInsn)
                }

                methodNode.instructions = insns
            }
        }
    }
}
