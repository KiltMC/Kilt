package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import com.llamalad7.mixinextras.sugar.Local
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.ParamPair

// Used for retargeting any local targets that were messed up by Neo's recompiling.
data class RetargetingLocalModifier(
    override val owner: String,
    override val methods: List<String>,
    val paramToLocalMapping: Map<ParamPair, Local>
) : MethodBasedModifier {
    companion object {
        val LOCAL = Type.getType(Local::class.java)
    }

    override lateinit var mappedOwner: String
    override lateinit var mappedMethods: List<String>

    fun retargetLocals(methodNode: MethodNode) {
        val paramAnnotations = methodNode.invisibleParameterAnnotations ?: return
        val splitDescriptor = Type.getArgumentTypes(methodNode.desc)
        val modifiedParamAnnotations = paramAnnotations.toMutableList()

        var hasModified = false
        for ((i, annotations) in paramAnnotations.withIndex()) {
            if (annotations == null)
                continue

            val descriptor = splitDescriptor[i]
            val newAnnotations = annotations.toMutableList()

            for ((j, annotation) in annotations.withIndex()) {
                if (annotation.desc == LOCAL.descriptor) {
                    val ordinal = KiltMixinModifications.annotationValuesToMap(annotation.values ?: listOf("ordinal", -1))["ordinal"] ?: -1
                    val mapping = paramToLocalMapping.filter { it.key.descriptor == descriptor.descriptor && it.key.ordinal == ordinal }
                    val remap = mapping.values.firstOrNull() ?: continue

                    newAnnotations[j] = AnnotationNode(Opcodes.ASM9, annotation.desc).apply {
                        this.values = KiltMixinModifications.mapToAnnotationValues(
                            mapOf(
                                "ordinal" to remap.ordinal,
                            )
                        )
                    }

                    hasModified = true
                }
            }

            modifiedParamAnnotations[i] = newAnnotations
        }

        if (hasModified) {
            methodNode.invisibleParameterAnnotations = modifiedParamAnnotations.toTypedArray()
        }
    }
}
