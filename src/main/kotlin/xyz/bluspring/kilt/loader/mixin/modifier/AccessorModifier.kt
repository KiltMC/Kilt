package xyz.bluspring.kilt.loader.mixin.modifier

import org.objectweb.asm.tree.MethodNode
import java.util.function.Function

data class AccessorModifier(
    val owner: String,
    val names: List<String>,
    val desc: String,
    val replacedMethod: Function<String, MethodNode>
) {
    lateinit var mappedOwner: String
    lateinit var mappedDesc: String
}