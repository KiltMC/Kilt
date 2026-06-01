package cpw.mods.modlauncher.api

interface ITransformerVotingContext {
    val className: String
    fun doesClassExist(): Boolean
    val initialClassSha256: ByteArray
    val auditActivities: MutableList<ITransformerActivity>
    val reason: String
    fun applyFieldPredicate(fieldPredicate: FieldPredicate): Boolean
    fun applyMethodPredicate(methodPredicate: MethodPredicate): Boolean
    fun applyClassPredicate(classPredicate: ClassPredicate): Boolean
    fun applyInstructionPredicate(insnPredicate: InsnPredicate): Boolean

    fun interface FieldPredicate {
        fun test(access: Int, name: String, descriptor: String, signature: String?, value: Any?): Boolean
    }

    fun interface MethodPredicate {
        fun test(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>): Boolean
    }

    fun interface ClassPredicate {
        fun test(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>): Boolean
    }

    fun interface InsnPredicate {
        fun test(insnCount: Int, opcode: Int, vararg args: Any?): Boolean
    }
}
