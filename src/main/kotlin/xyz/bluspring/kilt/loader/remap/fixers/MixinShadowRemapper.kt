package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.spongepowered.asm.mixin.Mixin
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper

object MixinShadowRemapper {
    val MIXIN_TYPE = Type.getType(Mixin::class.java)

    fun remapClass(classNode: ClassNode, remapper: KiltEnhancedRemapper) {
        val remappedFields = mutableMapOf<String, String>()
        val remappedMethods = mutableMapOf<String, String>()

        val mixinAnnotation = classNode.visibleAnnotations?.firstOrNull { it.desc == MIXIN_TYPE.descriptor }
            ?: classNode.invisibleAnnotations?.firstOrNull { it.desc == MIXIN_TYPE.descriptor }
            ?: throw IllegalStateException("Failed to locate mixin annotations!")
        val targetClassNames = mutableListOf<String>()

        val values = KiltMixinModifications.annotationValuesToMap(mixinAnnotation.values)

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

        // Collect all shadow fields
        for (field in classNode.fields) {
            val annotations = field.visibleAnnotations ?: field.invisibleAnnotations ?: continue

            if (annotations.none { isTargeted(it) })
                continue

            var remapped = ""

            for (className in targetClassNames) {
                remapped = remapper.mapFieldName(className, field.name, field.desc)

                if (remapped != field.name)
                    break
            }

            remappedFields[field.name] = remapped
            field.name = remapped
        }

        // Collect all shadow methods
        for (method in classNode.methods) {
            val annotations = method.visibleAnnotations ?: method.invisibleAnnotations ?: continue

            if (annotations.none { isTargeted(it) })
                continue

            var remapped = ""

            for (className in targetClassNames) {
                remapped = remapper.mapMethodName(className, method.name, method.desc)

                if (remapped != method.name)
                    break
            }

            remappedMethods[method.name] = remapped
            method.name = remapped
        }

        // Second pass, go through all the methods and point everything to the remapped shadows
        for (method in classNode.methods) {
            for (insnNode in method.instructions) {
                if (insnNode is FieldInsnNode) {
                    val remapped = remappedFields[insnNode.name] ?: continue
                    insnNode.name = remapped
                } else if (insnNode is MethodInsnNode) {
                    val remapped = remappedMethods[insnNode.name] ?: continue
                    insnNode.name = remapped
                }
            }
        }
    }

    private fun isTargeted(node: AnnotationNode): Boolean {
        return node.desc.contains("org/spongepowered/asm/mixin/Shadow") || node.desc.contains("org/spongepowered/asm/mixin/Overwrite")
    }
}