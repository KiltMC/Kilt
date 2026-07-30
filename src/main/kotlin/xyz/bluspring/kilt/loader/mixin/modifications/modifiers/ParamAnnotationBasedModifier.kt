package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.spongepowered.asm.mixin.transformer.ClassInfo
import xyz.bluspring.kilt.loader.remap.KiltRemapper

interface ParamAnnotationBasedModifier : AnnotationBasedModifier {

    fun modifyMixinParams(classInfo: ClassInfo, index: Int, parameter: Type, annotations: MutableList<AnnotationNode>)

    override fun modifyMixin(classInfo: ClassInfo, annotation: AnnotationNode, newAnnotations: MutableList<AnnotationNode>) {
        newAnnotations.add(annotation)
    }

    data class AddParamAnnotationModifier(
        override val owner: String,
        override val methods: List<String> = listOf(),
        override val variables: Map<String, Any> = mapOf(),
        val params: Map<ParamMatcher, AnnotationNode> = mapOf()
    ) : ParamAnnotationBasedModifier {
        override lateinit var mappedOwner: String
        override lateinit var mappedMethods: List<String>

        data class ParamMatcher(val index: Int? = null, private val type: String? = null) {
            val mappedType = if (type != null) KiltRemapper.remapClass(type) else type

            fun matches(index: Int, parameter: Type): Boolean {
                if (this.index != null && this.index == index) {
                    return true
                }
                if (this.mappedType != null && this.mappedType == parameter.internalName) {
                    return true
                }
                return false
            }
        }

        override fun modifyMixinParams(
            classInfo: ClassInfo,
            index: Int,
            parameter: Type,
            annotations: MutableList<AnnotationNode>
        ) {
            for ((param, annotation) in params) {
                if (param.matches(index, parameter)) {
                    annotations.add(annotation)
                }
            }
        }

    }

}
