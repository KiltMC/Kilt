package xyz.bluspring.kilt.loader.mixin.modifier

import com.bawnorton.mixinsquared.reflection.MixinInfoExtension
import com.bawnorton.mixinsquared.reflection.StateExtension
import com.bawnorton.mixinsquared.reflection.TargetClassContextExtension
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
import xyz.bluspring.kilt.Kilt

class KiltMixinModifier : IExtension {
    override fun checkActive(environment: MixinEnvironment): Boolean {
        return true
    }

    override fun preApply(context: ITargetClassContext) {
        // Ignore classes that aren't modified
        if (!KiltMixinModifications.MIXIN_CLASSES.contains(context.classInfo.name))
            return

        TargetClassContextExtension.tryAs(context).ifPresent { ext ->
            for (mixinInfo in ext.mixins) {
                val modId = mixinInfo.config.getDecoration<String>(FabricUtil.KEY_MOD_ID)

                // Ignore non-Forge mods
                if (!Kilt.loader.hasMod(modId))
                    continue

                val mixinClassNode = mixinInfo.getClassNode(0)
                var wasModified = false

                val replacedMethods = mutableMapOf<MethodNode, MethodNode>()

                for (methodNode in mixinClassNode.methods) {
                    val annotations = methodNode.visibleAnnotations ?: continue
                    val newAnnotations = mutableListOf<AnnotationNode>()

                    for (annotation in annotations) {
                        if (annotation.desc == ACCESSOR) {
                            val modifier = KiltMixinModifications.findMatchingAccessor(context.classInfo, annotation, methodNode)

                            if (modifier != null) {
                                val replacedMethod = modifier.replacedMethod.apply(mixinClassNode.name)

                                val newMethod = MethodNode()
                                newMethod.name = methodNode.name
                                newMethod.desc = methodNode.desc
                                newMethod.access = Opcodes.ACC_PUBLIC
                                newMethod.signature = methodNode.signature

                                newMethod.instructions = InsnList()
                                newMethod.instructions.insert(replacedMethod.instructions)

                                replacedMethods[methodNode] = newMethod

                                continue
                            }
                        }

                        val modifier = KiltMixinModifications.findMatchingModifier(context.classInfo, annotation)

                        if (modifier == null) {
                            newAnnotations.add(annotation)
                            continue
                        }

                        newAnnotations.addAll(modifier.replaceWith)
                        wasModified = true
                    }

                    if (wasModified) {
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
                    MixinInfoExtension.tryAs(mixinInfo)
                        .flatMap { StateExtension.tryAs(it.state) }
                        .ifPresent { it.setClassNode(mixinClassNode) }
                }
            }
        }
    }

    override fun postApply(context: ITargetClassContext) {
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
    }

    companion object {
        val ACCESSOR = Type.getDescriptor(Accessor::class.java)
    }
}