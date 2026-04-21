package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.InjectedShareAccessModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.RetargetingLocalModifier
import xyz.bluspring.kilt.util.KiltHelper

object MixinDirectModifierFixer {
    fun fixClass(classNode: ClassNode) {
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)
        val removedMethods = mutableListOf<MethodNode>()
        val addedMethods = mutableListOf<MethodNode>()

        for (targetClass in targetClassNames) {
            for (methodNode in classNode.methods) {
                val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
                for (annotation in annotations) {
                    val modifiers = KiltMixinModifications.findMatchingModifiers(targetClass, annotation, methodNode.desc)
                    val shareAccessModifiers = modifiers.filterIsInstance<InjectedShareAccessModifier>()

                    for (modifier in shareAccessModifiers) {
                        // We want to conflict as little as possible, so we're doing this.
                        val copiedMethodName = $$"kilt$modified_share_access$$${targetClass.split("/").last()}$$${methodNode.name}$$${annotation.desc.split("/").last().removeSuffix(";")}"

                        // Copy the original method entirely.
                        run {
                            val copiedMethod = MethodNode()
                            copiedMethod.name = copiedMethodName
                            copiedMethod.desc = methodNode.desc
                            copiedMethod.access = methodNode.access

                            copiedMethod.signature = methodNode.signature
                            copiedMethod.visibleAnnotations = methodNode.visibleAnnotations?.toMutableList()?.apply {
                                remove(annotation)
                            }
                            copiedMethod.invisibleAnnotations = methodNode.invisibleAnnotations?.toMutableList()?.apply {
                                remove(annotation)
                            }
                            copiedMethod.visibleTypeAnnotations = methodNode.visibleTypeAnnotations
                            copiedMethod.invisibleTypeAnnotations = methodNode.invisibleTypeAnnotations
                            copiedMethod.invisibleParameterAnnotations = methodNode.invisibleParameterAnnotations
                            copiedMethod.invisibleAnnotableParameterCount = methodNode.invisibleAnnotableParameterCount
                            copiedMethod.visibleAnnotableParameterCount = methodNode.visibleAnnotableParameterCount
                            copiedMethod.visibleLocalVariableAnnotations = methodNode.visibleLocalVariableAnnotations
                            copiedMethod.invisibleLocalVariableAnnotations = methodNode.invisibleLocalVariableAnnotations
                            copiedMethod.tryCatchBlocks = methodNode.tryCatchBlocks
                            copiedMethod.attrs = methodNode.attrs
                            copiedMethod.exceptions = methodNode.exceptions
                            copiedMethod.localVariables = methodNode.localVariables
                            copiedMethod.maxStack = methodNode.maxStack
                            copiedMethod.maxLocals = methodNode.maxLocals

                            copiedMethod.instructions = InsnList()
                            copiedMethod.instructions.insert(methodNode.instructions)

                            addedMethods.add(copiedMethod)
                        }

                        // Throw the handling over to our mixin modifier.
                        val newMethod = modifier.injectShareAccess(classNode, methodNode, annotation, copiedMethodName)

                        removedMethods.add(methodNode)
                        addedMethods.add(newMethod)
                    }

                    val retargetingLocalModifiers = modifiers.filterIsInstance<RetargetingLocalModifier>()
                    for (modifier in retargetingLocalModifiers) {
                        modifier.retargetLocals(methodNode)
                    }
                }
            }
        }

        classNode.methods.removeAll(removedMethods)
        classNode.methods.addAll(addedMethods)
    }
}