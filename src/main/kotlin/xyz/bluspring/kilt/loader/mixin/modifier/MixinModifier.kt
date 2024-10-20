package xyz.bluspring.kilt.loader.mixin.modifier

import org.objectweb.asm.tree.AnnotationNode

data class MixinModifier(
    val owner: String,
    val methods: List<String>,

    val variables: Map<String, Any> = mapOf(),

    val replaceWith: List<AnnotationNode>
) {
    lateinit var mappedOwner: String
}
