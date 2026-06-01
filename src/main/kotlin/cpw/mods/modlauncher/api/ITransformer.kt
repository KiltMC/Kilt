package cpw.mods.modlauncher.api

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

interface ITransformer<T> {
    companion object {
        @JvmField val DEFAULT_LABEL: Array<String> = arrayOf("default")
    }

    fun transform(input: T, context: ITransformerVotingContext): T
    fun castVote(context: ITransformerVotingContext): TransformerVoteResult
    fun targets(): Set<Target<T>>
    val targetType: TargetType<T>
    fun labels(): Array<String> = DEFAULT_LABEL

    @JvmRecord
    data class Target<T>(val className: String, val elementName: String, val elementDescriptor: String, val targetType: TargetType<T>) {
        companion object {
            @JvmStatic fun targetClass(className: String): Target<ClassNode> = Target(className, "", "", TargetType.CLASS)
            @JvmStatic fun targetPreClass(className: String): Target<ClassNode> = Target(className, "", "", TargetType.PRE_CLASS)
            @JvmStatic fun targetMethod(className: String, methodName: String, methodDescriptor: String): Target<MethodNode> = Target(className, methodName, methodDescriptor, TargetType.METHOD)
            @JvmStatic fun targetField(className: String, fieldName: String): Target<FieldNode> = Target(className, fieldName, "", TargetType.FIELD)
        }
    }
}
