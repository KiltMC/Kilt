package cpw.mods.modlauncher.api

import cpw.mods.modlauncher.TransformList
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.util.function.Supplier

data class TargetType<T>(private val name: String, val nodeType: Class<T>) {
    companion object {
        @JvmField val PRE_CLASS = TargetType("PRE_CLASS", ClassNode::class.java)
        @JvmField val CLASS = TargetType("CLASS", ClassNode::class.java)
        @JvmField val METHOD = TargetType("METHOD", MethodNode::class.java)
        @JvmField val FIELD = TargetType("FIELD", FieldNode::class.java)

        @JvmField val VALUES: Array<TargetType<*>> = arrayOf(PRE_CLASS, CLASS, METHOD, FIELD)

        @JvmStatic
        fun byName(name: String): TargetType<*> = VALUES.first { it.name == name }
    }

    operator fun get(transformers: Map<TargetType<*>, TransformList<*>>): TransformList<T> = transformers[this] as TransformList<T>
    fun mapSupplier(transformers: Map<TargetType<*>, TransformList<*>>): Supplier<TransformList<T>> = Supplier { this[transformers] }
}
