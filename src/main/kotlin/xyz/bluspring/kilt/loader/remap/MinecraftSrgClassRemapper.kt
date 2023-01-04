package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.ClassDef
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class MinecraftSrgClassRemapper(
    private val fabricNamespace: String,
    private val fabricClassDef: ClassDef,
    private val srgClassDef: ClassDef
) : ClassVisitor(Opcodes.ASM9) {
    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        val srgFieldName = srgClassDef.fields.firstOrNull {
            it.getName("intermediary") ==
                    if (fabricNamespace == "intermediary")
                        name
                    else
                        fabricClassDef.fields.firstOrNull { fieldDef ->
                            fieldDef.getName(fabricNamespace) == name
                        }?.getName("intermediary")
        }?.getName("srg")

        // I think this is how you add new fields? I'm not sure, honestly.
        // I had gone through a lot of googling to figure out how to do this,
        // not much info was given really.
        if (srgFieldName != null)
            super.visitField(access, srgFieldName, descriptor, signature, value).visitEnd()

        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val srgMethodName = srgClassDef.methods.firstOrNull {
            if (fabricNamespace == "intermediary")
                name == it.getName("intermediary") && descriptor == it.getDescriptor("intermediary")
            else
                fabricClassDef.methods.firstOrNull { methodDef ->
                    methodDef.getName(fabricNamespace) == name && methodDef.getDescriptor(fabricNamespace) == descriptor
                }.run {
                    this?.getName("intermediary") == it.getName("intermediary")
                            && this?.getDescriptor("intermediary") == it.getName("intermediary")
                }
        }?.getName("srg")

        val srgMethod = super.visitMethod(access, srgMethodName, descriptor, signature, exceptions)
        val types = Type.getMethodType(descriptor)

        srgMethod.visitVarInsn(Opcodes.ALOAD, 0)

        // Go through the params
        var i = 1
        types.argumentTypes.forEach { paramType ->
            when (paramType.elementType) {
                Type.BOOLEAN_TYPE, Type.BYTE_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE, Type.INT_TYPE -> {
                    srgMethod.visitVarInsn(Opcodes.ILOAD, i)
                }
                Type.LONG_TYPE -> {
                    srgMethod.visitVarInsn(Opcodes.LLOAD, i)
                }
                Type.FLOAT_TYPE -> {
                    srgMethod.visitVarInsn(Opcodes.FLOAD, i)
                }
                Type.DOUBLE_TYPE -> {
                    srgMethod.visitVarInsn(Opcodes.DLOAD, i)
                }
                else -> {
                    srgMethod.visitVarInsn(Opcodes.ALOAD, i)
                }
            }

            i++
        }

        srgMethod.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            fabricClassDef.getName(fabricNamespace),
            name, descriptor
        )

        if (types.returnType != Type.VOID_TYPE) {
            when (types.returnType) {
                Type.BOOLEAN_TYPE, Type.BYTE_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE, Type.INT_TYPE -> {
                    srgMethod.visitInsn(Opcodes.IRETURN)
                }
                Type.LONG_TYPE -> {
                    srgMethod.visitInsn(Opcodes.LRETURN)
                }
                Type.FLOAT_TYPE -> {
                    srgMethod.visitInsn(Opcodes.FRETURN)
                }
                Type.DOUBLE_TYPE -> {
                    srgMethod.visitInsn(Opcodes.DRETURN)
                }
                else -> {
                    srgMethod.visitInsn(Opcodes.ARETURN)
                }
            }
        }

        srgMethod.visitEnd()

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}