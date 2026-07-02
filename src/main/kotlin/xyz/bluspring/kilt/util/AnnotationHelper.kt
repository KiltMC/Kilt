package xyz.bluspring.kilt.util

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.spongepowered.asm.util.Annotations

object AnnotationHelper {

    fun getTargets(mixinAnnotation: AnnotationNode): Set<String> {
        val targets = Annotations.getValue<List<Type>?>(mixinAnnotation, "value")?.map { it.internalName }?.toMutableSet() ?: mutableSetOf()
        val additionalTargets = Annotations.getValue<List<String>?>(mixinAnnotation, "targets")
        if (additionalTargets != null) {
            targets.addAll(targets)
        }
        return targets
    }

}
