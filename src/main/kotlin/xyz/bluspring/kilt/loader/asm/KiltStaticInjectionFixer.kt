package xyz.bluspring.kilt.loader.asm

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import xyz.bluspring.fork.mm.api.ClassTinkerers
import xyz.bluspring.kilt.helpers.FabricImplemented
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import java.lang.annotation.ElementType
import java.lang.reflect.Modifier

object KiltStaticInjectionFixer {
    fun findAndFixInjections(mod: NeoForgeMod) {
        mod.scanData.getAnnotatedBy(FabricImplemented::class.java, ElementType.TYPE)
            .forEach { data ->
                ClassTinkerers.addTransformation(data.clazz.internalName) { classNode ->
                    for (methodNode in classNode.methods) {
                        if (methodNode.name == "<clinit>")
                            continue

                        if (!Modifier.isStatic(methodNode.access) || (methodNode.access and Opcodes.ACC_SYNTHETIC) != 0)
                            continue

                        val args = Type.getArgumentTypes(methodNode.desc)
                        val returnType = Type.getReturnType(methodNode.desc)

                        val originalLvt = methodNode.localVariables.toList()
                        methodNode.instructions.clear()

                        val l0 = Label()
                        val l1 = Label()
                        val l2 = Label()

                        methodNode.visitCode()

                        methodNode.visitLabel(l0)
                        var i = 0
                        for (type in args) {
                            methodNode.visitVarInsn(
                                when (type.descriptor) {
                                    "I", "Z", "S", "B", "C" -> Opcodes.ILOAD
                                    "J" -> Opcodes.LLOAD
                                    "D" -> Opcodes.DLOAD
                                    "F" -> Opcodes.FLOAD

                                    else -> Opcodes.ALOAD
                                }, i
                            )
                            i += type.size
                        }

                        methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, (data.annotationData()["value"]!! as Type).internalName, methodNode.name, methodNode.desc, false)

                        methodNode.visitLabel(l1)
                        methodNode.visitInsn(
                            when (returnType.descriptor) {
                                "I", "Z", "S", "B", "C" -> Opcodes.IRETURN
                                "J" -> Opcodes.LRETURN
                                "D" -> Opcodes.DRETURN
                                "F" -> Opcodes.FRETURN
                                "V" -> Opcodes.RETURN

                                else -> Opcodes.ARETURN
                            }
                        )

                        methodNode.visitLabel(l2)
                        i = 0
                        for ((index, type) in args.withIndex()) {
                            val lv = originalLvt[index]
                            methodNode.visitLocalVariable(lv.name, lv.desc, lv.signature, l0, l2, i)
                            i += type.size
                        }

                        methodNode.visitMaxs(0, 0)
                        methodNode.visitEnd()
                    }
                }
            }
    }
}
