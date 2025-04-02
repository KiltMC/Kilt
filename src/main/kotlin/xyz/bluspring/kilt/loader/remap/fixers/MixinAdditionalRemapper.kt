package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.spongepowered.asm.mixin.Mixin
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.KiltHelper

object MixinAdditionalRemapper {
    val MIXIN_TYPE = Type.getType(Mixin::class.java)
    // Able to match: Lpackage/class/name;methodName(BZLother/type/name;)V
    val MIXIN_METHOD_EXPLICIT_REGEX = Regex("(L(?:\\w+(/)?)*;)\\w+(?:\\((?:Z|B|C|S|I|J|F|D|L(?:\\w+(/)?)*;)*\\)(?:Z|B|C|S|I|J|F|D|V|L(?:\\w+(/)?)*;))?")

    fun remapClass(classNode: ClassNode, remapper: KiltEnhancedRemapper) {
        val remappedFields = mutableMapOf<String, String>()
        val remappedMethods = mutableMapOf<String, String>()

        val mixinAnnotation = KiltHelper.mergeNullableCollections(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .firstOrNull { it.desc == MIXIN_TYPE.descriptor }
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

        // Remap shadow and overwrite
        run {
            // Collect all shadow fields
            for (field in classNode.fields) {
                val annotations = KiltHelper.mergeNullableCollections(field.visibleAnnotations, field.invisibleAnnotations)

                if (annotations.none { isTargeted(it) })
                    continue

                var remapped = ""

                for (className in targetClassNames) {
                    // First pass, try to remap with current names
                    remapped = remapper.mapFieldName(className, field.name, field.desc)

                    if (remapped != field.name)
                        break

                    // Second pass, try to remap with original names
                    remapped = remapper.mapFieldName(KiltRemapper.unmapClass(className), field.name, KiltRemapper.remapDescriptor(field.desc, reverse = true))

                    if (remapped != field.name)
                        break
                }

                remappedFields[field.name] = remapped
                field.name = remapped
            }

            // Collect all shadow methods
            for (method in classNode.methods) {
                val annotations = KiltHelper.mergeNullableCollections(method.visibleAnnotations, method.invisibleAnnotations)

                if (annotations.none { isTargeted(it) })
                    continue

                var remapped = ""

                for (className in targetClassNames) {
                    // First pass, try to remap with original names
                    remapped = remapper.mapMethodName(className, method.name, method.desc)

                    if (remapped != method.name)
                        break

                    // Second pass, try to remap with original names
                    remapped = remapper.mapFieldName(KiltRemapper.unmapClass(className), method.name, KiltRemapper.remapDescriptor(method.desc, reverse = true))

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

        // Remove explicit class target in method
        run {
            for (method in classNode.methods) {
                val annotations = KiltHelper.mergeNullableCollections(method.visibleAnnotations, method.invisibleAnnotations)

                for (node in annotations) {
                    if (node.values == null)
                        continue

                    var wasModified = false
                    val values = KiltMixinModifications.annotationValuesToMap(node.values).toMutableMap()

                    if (values.contains("method")) {
                        val methodValue = values["method"]!!

                        if (methodValue is String) {
                            val owner = tryGetOwnerFromMethodValue(methodValue) ?: continue
                            values["method"] = methodValue.removePrefix(owner)
                            wasModified = true
                        } else if (methodValue is List<*>) {
                            val list = mutableListOf<Any?>()

                            for (value in methodValue) {
                                if (value !is String) {
                                    list.add(value)
                                    continue
                                }

                                val owner = tryGetOwnerFromMethodValue(value)

                                if (owner == null) {
                                    list.add(value)
                                    continue
                                }

                                list.add(value.removePrefix(owner))
                                wasModified = true
                            }

                            values["method"] = list
                        }
                    }

                    if (wasModified) {
                        node.values = KiltMixinModifications.mapToAnnotationValues(values)
                    }
                }
            }
        }
    }

    private fun tryGetOwnerFromMethodValue(name: String): String? {
        if (!name.startsWith("L"))
            return null

        val owner = name.replaceAfter(";", "")
        if (owner == name)
            return null

        return owner
    }

    private fun isTargeted(node: AnnotationNode): Boolean {
        return node.desc.contains("org/spongepowered/asm/mixin/Shadow") || node.desc.contains("org/spongepowered/asm/mixin/Overwrite")
    }
}