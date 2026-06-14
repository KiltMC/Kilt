package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_CLASS_NAME
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_METHOD_DESCRIPTOR
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentRemapper
import xyz.bluspring.kilt.util.KiltHelper
import java.util.*

// Remap shadow and overwrite
object MixinShadowRemapper {
    private val targetClassNodeCache = Collections.synchronizedMap<String, ClassNode>(mutableMapOf())

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

    data class MethodReference(val name: String, val desc: String)

    private fun updateFieldsAndMethods(
        classNode: ClassNode, remapper: KiltEnhancedRemapper,
        remappedFields: MutableMap<String, String>,
        remappedMethods: MutableMap<MethodReference, String>,
        targetClassNames: Collection<String>,
        unmappedParentMixinLookup: Map<String, ClassNode>, renameNodes: Boolean,
    ) {
        val targetClassNodes = mutableListOf<ClassNode>()

        // Need to get the original target class so we can see what the shadow's annotations look like.
        // This is just so we apply an @OnlyIn on the shadow so we don't crash on dedicated server.
        for (className in targetClassNames) {
            val normalizedClassName = className.replace(".", "/").removeSurrounding("L", ";")

            synchronized(targetClassNodeCache) {
                if (targetClassNodeCache.contains(normalizedClassName)) {
                    targetClassNodes.add(targetClassNodeCache[normalizedClassName]!!)
                    continue
                }
            }

            val targetClassStream = remapper.provider.getClassStream(normalizedClassName)
                ?: continue

            val classReader = ClassReader(targetClassStream)
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            targetClassNodes.add(classNode)
            this.targetClassNodeCache[normalizedClassName] = classNode
        }

        // Collect all shadow fields
        for (field in classNode.fields) {
            // Try to apply an @OnlyIn on the shadow field target, if one exists on the parent class.
            run {
                val annotations = KiltHelper.mergeNullableCollections(field.visibleAnnotations, field.invisibleAnnotations)
                if (annotations.any { it.desc == EnvironmentRemapper.ONLYIN_TYPE.descriptor || it.desc == EnvironmentRemapper.ONLYINS_TYPE.descriptor })
                    return@run

                for (targetClass in targetClassNodes) {
                    val targetField = targetClass.fields.firstOrNull { it.name == field.name && it.desc == field.desc }
                        ?: continue

                    val targetAnnotations = KiltHelper.mergeNullableCollections(targetField.visibleAnnotations, targetField.invisibleAnnotations)
                    val fieldAnnotations = field.visibleAnnotations?.toMutableList() ?: mutableListOf()

                    fieldAnnotations.add(targetAnnotations.firstOrNull { it.desc == EnvironmentRemapper.ONLYIN_TYPE.descriptor || it.desc == EnvironmentRemapper.ONLYINS_TYPE.descriptor }
                        ?: continue)

                    field.visibleAnnotations = fieldAnnotations
                }
            }

            val remapped = remapField(field, remapper, targetClassNames) ?: continue

            remappedFields[field.name] = remapped
            if (renameNodes) {
                field.name = remapped
            }
        }

        // Collect all shadow methods
        for (method in classNode.methods) {
            // Try to apply an @OnlyIn on the shadow method target, if one exists on the parent class.
            run {
                val annotations = KiltHelper.mergeNullableCollections(method.visibleAnnotations, method.invisibleAnnotations)
                if (annotations.any { it.desc == EnvironmentRemapper.ONLYIN_TYPE.descriptor || it.desc == EnvironmentRemapper.ONLYINS_TYPE.descriptor })
                    return@run

                for (targetClass in targetClassNodes) {
                    val targetMethod = targetClass.methods.firstOrNull { it.name == method.name && it.desc == method.desc }
                        ?: continue

                    val targetAnnotations = KiltHelper.mergeNullableCollections(targetMethod.visibleAnnotations, targetMethod.invisibleAnnotations)
                    val fieldAnnotations = method.visibleAnnotations?.toMutableList() ?: mutableListOf()

                    fieldAnnotations.add(targetAnnotations.firstOrNull { it.desc == EnvironmentRemapper.ONLYIN_TYPE.descriptor || it.desc == EnvironmentRemapper.ONLYINS_TYPE.descriptor }
                        ?: continue)

                    method.visibleAnnotations = fieldAnnotations
                }
            }

            val remapped = remapMethod(method, remapper, targetClassNames) ?: continue

            remappedMethods[MethodReference(method.name, method.desc)] = remapped
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
        val remappedMethods = mutableMapOf<MethodReference, String>()
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
                    val remapped = remappedMethods[MethodReference(insnNode.name, insnNode.desc)] ?: continue
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
                                    remappedMethods[MethodReference(lambdaTarget.name, lambdaTarget.desc)] ?: continue,
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

    fun clearCache() {
        this.targetClassNodeCache.clear()
    }

    private fun isTargeted(node: AnnotationNode): Boolean {
        return node.desc.contains("org/spongepowered/asm/mixin/Shadow") || node.desc.contains("org/spongepowered/asm/mixin/Overwrite")
    }
}
