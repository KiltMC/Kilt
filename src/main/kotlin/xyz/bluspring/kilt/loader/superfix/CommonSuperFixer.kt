package xyz.bluspring.kilt.loader.superfix

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object CommonSuperFixer {
    fun fixClass(classNode: ClassNode) {
        // Let's make sure it doesn't already exist
        if (
            classNode.access and Opcodes.ACC_INTERFACE == 0
            && classNode.methods.none { it.name == "<init>" && (it.signature == "()V" || it.desc == "()V") }
            && classNode.name.endsWith("Event") // this is stupid, but it'll work.
        ) {
            val method = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", "()V", null)
            method.visitCode()
            method.visitVarInsn(Opcodes.ALOAD, 0)
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false)

            // init everything with null
            classNode.fields.forEach { field ->
                if (field.access and Opcodes.ACC_FINAL != 0) {
                    when (field.desc) {
                        "I" -> {
                            method.visitVarInsn(Opcodes.ILOAD, 0)
                            method.visitInsn(Opcodes.ICONST_M1)
                        }

                        "F" -> {
                            method.visitVarInsn(Opcodes.FLOAD, 0)
                            method.visitInsn(Opcodes.FCONST_0)
                        }

                        "D" -> {
                            method.visitVarInsn(Opcodes.DLOAD, 0)
                            method.visitInsn(Opcodes.DCONST_0)
                        }

                        "J" -> {
                            method.visitVarInsn(Opcodes.LLOAD, 0)
                            method.visitInsn(Opcodes.LCONST_0)
                        }

                        else -> {
                            method.visitVarInsn(Opcodes.ALOAD, 0)
                            method.visitInsn(Opcodes.ACONST_NULL)
                        }
                    }
                    method.visitFieldInsn(Opcodes.PUTFIELD, classNode.name, field.name, field.desc)
                }
            }

            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(0, 0)
            method.visitEnd()
        }
    }
}