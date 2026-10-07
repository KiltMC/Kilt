package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import org.spongepowered.asm.mixin.injection.Inject
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.MixinRefmap
import xyz.bluspring.kilt.util.KiltHelper

object MixinRemapper {
    fun getMixinClassTargets(
        classNode: ClassNode,
        mixinAnnotation: AnnotationNode = KiltHelper.mergeNullableCollections(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .firstOrNull { it.desc == MixinTypes.MIXIN.descriptor }
            ?: throw IllegalStateException("Failed to locate mixin annotations!"),
        values: Map<String, Any> = MixinHelpers.annotationValuesToMap(mixinAnnotation.values)
    ): Collection<String> {
        val targetClassNames = mutableListOf<String>()

        if (values.contains("value")) {
            if (values["value"] is List<*>) {
                targetClassNames.addAll((values["value"] as List<Type>).map { it.internalName })
            } else if (values["value"] is Type) {
                targetClassNames.add((values["value"] as Type).internalName)
            }
        }

        if (values.contains("targets")) {
            if (values["targets"] is List<*>) {
                targetClassNames.addAll((values["targets"] as List<String>).map { it.replace(".", "/").removeSurrounding("L", ";") })
            } else if (values["targets"] is String) {
                targetClassNames.add((values["targets"] as String).replace(".", "/").removeSurrounding("L", ";"))
            }
        }

        return targetClassNames
    }
}
