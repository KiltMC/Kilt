package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Handle
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_CLASS_NAME
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_METHOD_DESCRIPTOR
import xyz.bluspring.kilt.util.KiltHelper
import kotlin.collections.set

// Remap shadow and overwrite
object MixinShadowRemapper {

    private fun remapField(field: FieldNode, remapper: KiltEnhancedRemapper, targetClassNames: Collection<String>): String? {
        val annotations = KiltHelper.mergeNullableCollections(field.visibleAnnotations, field.invisibleAnnotations)

        if (annotations.none { isTargeted(it) })
            return null

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
        return remapped
    }

    private fun remapMethod(method: MethodNode, remapper: KiltEnhancedRemapper, targetClassNames: Collection<String>): String? {
        val annotations = KiltHelper.mergeNullableCollections(method.visibleAnnotations, method.invisibleAnnotations)

        if (annotations.none { isTargeted(it) })
            return null

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
        return remapped
    }

    private fun updateFieldsAndMethods(
        classNode: ClassNode, remapper: KiltEnhancedRemapper,
        remappedFields: MutableMap<String, String>,
        remappedMethods: MutableMap<String, String>,
        targetClassNames: Collection<String>,
        unmappedParentMixinLookup: Map<String, ClassNode>, renameNodes: Boolean,
    ) {
        // Collect all shadow fields
        for (field in classNode.fields) {
            val remapped = remapField(field, remapper, targetClassNames) ?: continue

            remappedFields[field.name] = remapped
            if (renameNodes) {
                field.name = remapped
            }
        }
        // Collect all shadow methods
        for (method in classNode.methods) {
            val remapped = remapMethod(method, remapper, targetClassNames) ?: continue

            remappedMethods[method.name] = remapped
            if (renameNodes) {
                method.name = remapped
            }
        }

        // Handle parent classes. We don't want to actually remap them here, but we want to collect any shadows they might contain.
        if (classNode.superName != null) {
            val superNode = unmappedParentMixinLookup[classNode.superName]
            if (superNode != null) {
                updateFieldsAndMethods(superNode, remapper, remappedFields, remappedMethods, targetClassNames, unmappedParentMixinLookup, false)
            }
        }
        if (classNode.interfaces != null) {
            for (iface in classNode.interfaces) {
                val ifaceNode = unmappedParentMixinLookup[iface]
                if (ifaceNode != null) {
                    updateFieldsAndMethods(ifaceNode, remapper, remappedFields, remappedMethods, targetClassNames, unmappedParentMixinLookup, false)
                }
            }
        }
    }

    fun remapClass(classNode: ClassNode, remapper: KiltEnhancedRemapper, unmappedParentMixinLookup: Map<String, ClassNode>) {
        val remappedFields = mutableMapOf<String, String>()
        val remappedMethods = mutableMapOf<String, String>()
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)

        // Collect shadows in this class
        updateFieldsAndMethods(classNode, remapper, remappedFields, remappedMethods, targetClassNames, unmappedParentMixinLookup, true)

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
