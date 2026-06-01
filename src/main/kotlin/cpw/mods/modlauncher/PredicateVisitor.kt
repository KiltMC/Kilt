package cpw.mods.modlauncher

import cpw.mods.modlauncher.api.ITransformerVotingContext
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

open class PredicateVisitor() : ClassVisitor(Opcodes.ASM9) {
    private var methodPredicate: ITransformerVotingContext.MethodPredicate? = null
    private var fieldPredicate: ITransformerVotingContext.FieldPredicate? = null
    private var classPredicate: ITransformerVotingContext.ClassPredicate? = null
    internal var result = false

    constructor(methodPredicate: ITransformerVotingContext.MethodPredicate) : this() {
        this.methodPredicate = methodPredicate
    }

    constructor(fieldPredicate: ITransformerVotingContext.FieldPredicate) : this() {
        this.fieldPredicate = fieldPredicate
    }

    constructor(classPredicate: ITransformerVotingContext.ClassPredicate) : this() {
        this.classPredicate = classPredicate
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        result = fieldPredicate == null || fieldPredicate!!.test(access, name, descriptor, signature, value)
        return null
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
        result = methodPredicate == null || methodPredicate!!.test(access, name, descriptor, signature, exceptions ?: arrayOf())
        return null
    }

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
        result = classPredicate == null || classPredicate!!.test(version, access, name, signature, superName, interfaces ?: arrayOf())
    }
}
