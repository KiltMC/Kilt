package xyz.bluspring.kilt.loader.remap.fixers.mixin

import com.llamalad7.mixinextras.sugar.Share
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.injection.Inject
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.ParamPair
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.InjectedShareAccessModifier
import xyz.bluspring.kilt.util.KiltHelper

object MixinShareAccessFixer {
    fun fixClass(classNode: ClassNode) {
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)
        val removedMethods = mutableListOf<MethodNode>()
        val addedMethods = mutableListOf<MethodNode>()

        for (targetClass in targetClassNames) {
            for (methodNode in classNode.methods) {
                val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
                for (annotation in annotations) {
                    val modifier = KiltMixinModifications.findMatchingModifier(targetClass, annotation, methodNode.desc)

                    if (modifier !is InjectedShareAccessModifier)
                        continue

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
            }
        }

        classNode.methods.removeAll(removedMethods)
        classNode.methods.addAll(addedMethods)
    }
}