package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import org.objectweb.asm.tree.AnnotationNode
import org.spongepowered.asm.mixin.transformer.ClassInfo
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinRemapper

sealed interface AnnotationBasedModifier : MixinModifier {
    val methods: List<String>
    val variables: Map<String, Any>

    fun modifyMixin(classInfo: ClassInfo, annotation: AnnotationNode, newAnnotations: MutableList<AnnotationNode>)

    data class ReplacedAnnotationsModifier(
        override val owner: String,
        override val methods: List<String> = listOf(),
        override val variables: Map<String, Any> = mapOf(),

        val replaceWith: List<AnnotationNode> = listOf()
    ) : AnnotationBasedModifier {
        override lateinit var mappedOwner: String

        override fun modifyMixin(classInfo: ClassInfo, annotation: AnnotationNode, newAnnotations: MutableList<AnnotationNode>) {
            if (annotation.desc == KiltMixinModifications.SUGAR_WRAPPER.descriptor) {
                val list = this.replaceWith

                if (list.size == 1) {
                    val map = KiltMixinModifications.annotationValuesToMap(annotation.values).toMutableMap()
                    map["original"] = list[0]
                    annotation.values = KiltMixinModifications.mapToAnnotationValues(map)
                    newAnnotations.add(annotation)
                } else {
                    val map = KiltMixinModifications.annotationValuesToMap(annotation.values).toMutableMap()

                    for (node in list) {
                        if (node.desc.contains("mixinsquared"))
                            newAnnotations.add(node)
                        else
                            map["original"] = node
                    }

                    annotation.values = KiltMixinModifications.mapToAnnotationValues(map)
                    newAnnotations.add(annotation)
                }
            } else {
                newAnnotations.addAll(this.replaceWith)
            }
        }
    }

    data class NameRemappingAnnotationModifier(
        override val owner: String,
        override val methods: List<String> = listOf(),
        override val variables: Map<String, Any> = mapOf(),

        val remapMethodsTo: String,
    ) : AnnotationBasedModifier {
        override lateinit var mappedOwner: String

        override fun modifyMixin(classInfo: ClassInfo, annotation: AnnotationNode, newAnnotations: MutableList<AnnotationNode>) {
            newAnnotations.add(
                KiltMixinModifications.createAnnotation(annotation.desc,
                    KiltMixinModifications.annotationValuesToMap(annotation.values).toMutableMap().apply {
                        this["method"] = listOf(
                            MixinRemapper.remapTargetString(remapMethodsTo, listOf(KiltRemapper.unmapClass(classInfo.name)), KiltRemapper.enhancedRemapper)
                        )
                    })
            )
        }
    }
}
