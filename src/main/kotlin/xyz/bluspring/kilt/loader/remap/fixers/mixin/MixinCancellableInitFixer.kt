package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.injection.Inject
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifications
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.reflect.Modifier
import kotlin.collections.contains

// Some Forge mods have an @Inject into <init> and <clinit> calls while also having cancellable = true.
// Forge's mixin supports this (despite it not actually doing anything), but Fabric's mixin does not.
object MixinCancellableInitFixer {
    private val INJECT_TYPE = Type.getType(Inject::class.java)

    fun fixClass(classNode: ClassNode) {
        for (methodNode in classNode.methods) {
            val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)

            for (annotationNode in annotations) {
                if (annotationNode.values == null || annotationNode.values.isEmpty())
                    continue

                // Only target @Inject methods.
                if (annotationNode.desc != INJECT_TYPE.descriptor)
                    continue

                var wasModified = false
                val values = KiltMixinModifications.annotationValuesToMap(annotationNode.values).toMutableMap()

                // We should be able to just do an annotation scan and not have to check whether it is actually used, because <init> and <clinit> are not supposed to be
                // cancelled either way.
                if (values.contains("cancellable") && values["cancellable"] == true) {
                    if (values.contains("method")) {
                        val methodValue = values["method"]

                        if (methodValue is String && (methodValue == "<init>" || methodValue == "<clinit>")) {
                            values.remove("cancellable")
                            wasModified = true
                        } else if (methodValue is List<*> && methodValue.any { it is String && (it == "<init>" || it == "<clinit>") }) {
                            values.remove("cancellable")
                            wasModified = true
                        }
                    }
                }

                if (wasModified) {
                    annotationNode.values = KiltMixinModifications.mapToAnnotationValues(values)
                }
            }
        }
    }
}