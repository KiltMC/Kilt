package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import java.util.function.Function

data class AccessorModifier(
    override val owner: String,
    val names: List<String>,
    val desc: String,

    /**
     * This should be an interface!
     */
    val remappedOwner: String,
    val remappedName: String
) : MixinModifier {
    override lateinit var mappedOwner: String
    lateinit var mappedDesc: String

    fun remapAccessor(owner: String): MethodNode = MethodNode().apply {
        visitCode()

        val label0 = Label()
        val label1 = Label()

        visitLabel(label0)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitTypeInsn(Opcodes.CHECKCAST, remappedOwner)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            remappedOwner,
            remappedName, mappedDesc, true
        )
        visitInsn(Opcodes.ARETURN)

        visitLabel(label1)
        visitLocalVariable("this", "L${owner};", null, label0, label1, 0)
        visitMaxs(1, 1)

        visitEnd()
    }
}