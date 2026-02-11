package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Handle
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_CLASS_NAME
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_METHOD_DESCRIPTOR
import xyz.bluspring.kilt.util.KiltHelper
import kotlin.collections.set

// Remap shadow and overwrite
object MixinShadowRemapper {
    fun remapClass(classNode: ClassNode, remapper: KiltEnhancedRemapper) {
        val remappedFields = mutableMapOf<String, String>()
        val remappedMethods = mutableMapOf<String, String>()
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)

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
                remapped = remapper.mapMethodName(KiltRemapper.unmapClass(className), method.name, KiltRemapper.remapDescriptor(method.desc, reverse = true))

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
                } else if (insnNode is InvokeDynamicInsnNode) {
                    if ("metafactory" != insnNode.bsm.name)
                        continue

                    if (LAMBDA_CLASS_NAME != insnNode.bsm.owner)
                        continue

                    if (LAMBDA_METHOD_DESCRIPTOR != insnNode.bsm.desc)
                        continue

                    if (insnNode.bsmArgs?.size == 3) {
                        if (insnNode.bsmArgs[1] is Handle) {
                            val lambdaTarget = insnNode.bsmArgs[1] as Handle
                            if (lambdaTarget.owner == classNode.name) {
                                val handle = Handle(lambdaTarget.tag, lambdaTarget.owner,
                                    remappedMethods[lambdaTarget.name] ?: continue,
                                    lambdaTarget.desc, lambdaTarget.isInterface
                                )

                                insnNode.bsmArgs[1] = handle
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isTargeted(node: AnnotationNode): Boolean {
        return node.desc.contains("org/spongepowered/asm/mixin/Shadow") || node.desc.contains("org/spongepowered/asm/mixin/Overwrite")
    }
}