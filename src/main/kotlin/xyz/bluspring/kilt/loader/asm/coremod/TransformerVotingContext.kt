package xyz.bluspring.kilt.loader.asm.coremod

import cpw.mods.modlauncher.PredicateVisitor
import cpw.mods.modlauncher.api.ITransformerActivity
import cpw.mods.modlauncher.api.ITransformerVotingContext
import org.objectweb.asm.tree.*

class TransformerVotingContext(
    override val className: String,
    private val classExists: Boolean,
    private val sha256: () -> ByteArray,
    override val auditActivities: MutableList<ITransformerActivity>,
    override val reason: String,
) : ITransformerVotingContext {
    internal lateinit var node: Any

    override fun doesClassExist(): Boolean = this.classExists

    override val initialClassSha256: ByteArray
        get() = this.sha256()

    override fun applyFieldPredicate(fieldPredicate: ITransformerVotingContext.FieldPredicate): Boolean {
        val node = this.node
        if (node is FieldNode) {
            val visitor = PredicateVisitor(fieldPredicate)
            node.accept(visitor)
            return visitor.result
        }

        return false
    }

    override fun applyMethodPredicate(methodPredicate: ITransformerVotingContext.MethodPredicate): Boolean {
        val node = this.node
        if (node is MethodNode) {
            val visitor = PredicateVisitor(methodPredicate)
            node.accept(visitor)
            return visitor.result
        }

        return false
    }

    override fun applyClassPredicate(classPredicate: ITransformerVotingContext.ClassPredicate): Boolean {
        val node = this.node
        if (node is ClassNode) {
            val visitor = PredicateVisitor(classPredicate)
            node.accept(visitor)
            return visitor.result
        }

        return false
    }

    override fun applyInstructionPredicate(insnPredicate: ITransformerVotingContext.InsnPredicate): Boolean {
        val node = this.node
        if (node is MethodNode) {
            var result = false
            node.instructions.forEachIndexed { index, insn ->
                result = result or insnPredicate.test(index, insn.opcode, *toObjectArray(insn))
            }

            return result
        }

        return false
    }

    private fun toObjectArray(insn: AbstractInsnNode): Array<Any?> {
        return when (insn) {
            is MethodInsnNode -> arrayOf(insn.name, insn.desc, insn.owner, insn.desc)
            is FieldInsnNode -> arrayOf(insn.name, insn.desc, insn.owner)
            else -> emptyArray()
        }
    }
}
