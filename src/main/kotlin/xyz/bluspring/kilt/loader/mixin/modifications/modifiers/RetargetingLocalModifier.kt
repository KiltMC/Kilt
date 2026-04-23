package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import com.llamalad7.mixinextras.sugar.Local
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.LocalPair

// Used for retargeting any local targets that were messed up by Neo's recompiling.
data class RetargetingLocalModifier(
    override val owner: String,
    override val methods: List<String>,
    val paramToLocalMapping: Map<LocalPair, Local>
) : MethodBasedModifier {
    companion object {
        val LOCAL = Type.getType(Local::class.java)
    }

    override lateinit var mappedOwner: String
    override lateinit var mappedMethods: List<String>

    private fun <T> matches(value: T?, target: T, default: T): Boolean {
        if (value == null)
            return target == default

        if (value is Collection<*> && target is Collection<*>) {
            if (value.isEmpty() && target.isEmpty())
                return true

            return value.containsAll(target)
        }

        if (value is Array<*> && target is Array<*>) {
            if (value.isEmpty() && target.isEmpty())
                return true

            return value.contentEquals(target)
        }

        return value == target
    }

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
                    val values = KiltMixinModifications.annotationValuesToMap(annotation.values ?: listOf())
                    val ordinal = values["ordinal"]
                    val print = values["print"]
                    val index = values["index"]
                    val name = values["name"]
                    val argsOnly = values["argsOnly"]
                    val type = values["type"]

                    val mapping = paramToLocalMapping.filter { it.key.descriptor == descriptor.descriptor
                            && (matches(ordinal, it.key.local.ordinal, -1)
                            && matches(print, it.key.local.print, false)
                            && matches(index, it.key.local.index, -1)
                            && matches(name, it.key.local.name.toList(), listOf<String>())
                            && matches(argsOnly, it.key.local.argsOnly, false)
                            && matches(type, it.key.local.type, Unit::class)
                        )
                    }
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
