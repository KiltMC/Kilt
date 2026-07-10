package xyz.bluspring.kilt.loader.mixin.modifications

import com.bawnorton.mixinsquared.reflection.MixinInfoExtension
import com.bawnorton.mixinsquared.reflection.StateExtension
import com.bawnorton.mixinsquared.reflection.TargetClassContextExtension
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.InjectedShareAccessModifier
import xyz.bluspring.kilt.loader.remap.MixinTypes

class KiltMixinModifier : IExtension {
    override fun checkActive(environment: MixinEnvironment): Boolean {
        return true
    }

    override fun preApply(context: ITargetClassContext) {
        // Ignore classes that aren't modified
        if (!KiltMixinModifications.MIXIN_CLASSES.contains(context.classInfo.name))
            return

        TargetClassContextExtension.tryAs(context) { ext ->
            for (mixinInfo in ext.mixins) {
                val modId = mixinInfo.config.getDecoration<String>(FabricUtil.KEY_MOD_ID)

                // Essential mod causes a null modId to appear
                if (modId == null || !Kilt.loader.hasMod(modId))
                    // Ignore non-Forge mods
                    continue

                val mixinClassNode = mixinInfo.getClassNode(0)
                var wasModified = false

                val replacedMethods = mutableMapOf<MethodNode, MethodNode>()

                for (methodNode in mixinClassNode.methods) {
                    val annotations = methodNode.visibleAnnotations ?: continue
                    val newAnnotations = mutableListOf<AnnotationNode>()

                    modifierApplier@for (annotation in annotations) {
                        if (annotation.desc == MixinTypes.ACCESSOR.descriptor) {
                            val modifier = KiltMixinModifications.findMatchingAccessor(context.classInfo, annotation, methodNode)

                            if (modifier != null) {
                                val replacedMethod = modifier.remapAccessor(mixinClassNode.name)

                                val newMethod = MethodNode()
                                newMethod.name = methodNode.name
                                newMethod.desc = methodNode.desc

                                if (replacedMethod.access != 0)
                                    newMethod.access = replacedMethod.access
                                else
                                    newMethod.access = (methodNode.access and Opcodes.ACC_ABSTRACT.inv()) // remove abstract

                                newMethod.signature = methodNode.signature

                                newMethod.instructions = InsnList()
                                newMethod.instructions.insert(replacedMethod.instructions)

                                replacedMethods[methodNode] = newMethod

                                continue
                            }
                        }

                        val modifiers = KiltMixinModifications.findMatchingModifiers(context.classInfo.name, annotation, methodNode.desc)

                        if (modifiers.isEmpty()) {
                            newAnnotations.add(annotation)
                            continue
                        }

                        for (modifier in modifiers) {
                            when (modifier) {
                                is AnnotationBasedModifier -> {
                                    modifier.modifyMixin(context.classInfo, annotation, newAnnotations)
                                    wasModified = true
                                }

                                is InjectedShareAccessModifier -> {
                                    // This modifier does nothing here, it's in the fixers instead.
                                    //newAnnotations.add(annotation)
                                    continue
                                }
                            }
                        }
                    }

                    if (wasModified) {
                        methodNode.visibleAnnotations = mutableListOf()
                        methodNode.visibleAnnotations.clear()
                        methodNode.visibleAnnotations.addAll(newAnnotations)
                    }
                }

                if (replacedMethods.isNotEmpty()) {
                    for ((original, replaced) in replacedMethods) {
                        mixinClassNode.methods.remove(original)
                        mixinClassNode.methods.add(replaced)
                    }

                    wasModified = true
                }

                if (wasModified) {
                    MixinInfoExtension.tryAs(mixinInfo) {
                        StateExtension.tryAs(it.state) { state ->
                            state.setClassNode(mixinClassNode)
                        }
                    }
                }
            }
        }
    }

    override fun postApply(context: ITargetClassContext) {
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
    }
}
