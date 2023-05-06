package xyz.bluspring.kilt.loader.superfix

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object CommonSuperFixer {
    fun fixClass(classNode: ClassNode) {
        // Let's make sure it doesn't already exist
        if (
            classNode.access and Opcodes.ACC_INTERFACE == 0
            && classNode.methods.none { it.name == "<init>" && (it.signature == "()V" || it.desc == "()V") }
            &&
            ((
                    (classNode.visibleAnnotations != null && classNode.visibleAnnotations.any { it.desc.contains("EventBusSubscriber") }) ||
                    (classNode.methods.any {
                        it.visibleAnnotations != null && it.visibleAnnotations.any { a ->
                            a.desc.contains("SubscribeEvent")
                        }
                    }
                    // *sigh*
            )) || classNode.name.endsWith("Event"))
        ) {
            val method = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", "()V", null)
            method.visitCode()
            val label0 = Label()

            method.visitLabel(label0)
            method.visitVarInsn(Opcodes.ALOAD, 0)
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false)

            // init everything with null
            classNode.fields.forEach { field ->
                if (field.access and Opcodes.ACC_FINAL != 0) {
                    val label = Label()

                    method.visitLabel(label)
                    method.visitVarInsn(Opcodes.ALOAD, 0)

                    when (field.desc) {
                        "I" -> {
                            method.visitInsn(Opcodes.ICONST_M1)
                        }

                        "F" -> {
                            method.visitInsn(Opcodes.FCONST_0)
                        }

                        "D" -> {
                            method.visitInsn(Opcodes.DCONST_0)
                        }

                        "J" -> {
                            method.visitInsn(Opcodes.LCONST_0)
                        }

                        else -> {
                            method.visitInsn(Opcodes.ACONST_NULL)
                        }
                    }
                    method.visitFieldInsn(Opcodes.PUTFIELD, classNode.name, field.name, field.desc)
                }
            }

            method.visitLabel(Label())
            method.visitInsn(Opcodes.RETURN)

            val lastLabel = Label()

            method.visitLabel(lastLabel)
            method.visitLocalVariable("this", "L${classNode.name};", null, label0, lastLabel, 0)

            method.visitMaxs(0, 0)
            method.visitEnd()
        }
    }
}