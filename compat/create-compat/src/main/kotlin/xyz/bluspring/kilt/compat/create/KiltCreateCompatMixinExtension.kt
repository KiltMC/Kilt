package xyz.bluspring.kilt.compat.create

import com.bawnorton.mixinsquared.reflection.MixinInfoExtension
import com.bawnorton.mixinsquared.reflection.StateExtension
import com.bawnorton.mixinsquared.reflection.TargetClassContextExtension
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_CLASS_NAME
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentLambdaFixer.LAMBDA_METHOD_DESCRIPTOR
import kotlin.collections.set

class KiltCreateCompatMixinExtension : IExtension {
    companion object {
        const val TINY_TATER_TOKEN = "xyz/bluspring/kilt/compat/create/registrate/FluidTypeFactoryToken"
        const val FLUID_TYPE_FACTORY_INTERFACE = "com/tterrag/registrate/builders/FluidBuilder\$FluidTypeFactory"

        fun shouldRemap(mixinInfo: IMixinInfo?, method: MethodNode): Boolean {
            if (method.desc.contains("L$TINY_TATER_TOKEN;")) return true
            if (method.name.equals($$"kilt$create") && mixinInfo?.name == "registrate_fabric.FluidBuilderMixin") return true
            if (method.name.equals("createFluidBuilder") && mixinInfo?.name == "registrate_fabric.FluidBuilderHelperMixin") return true
            return false
        }

        fun modifyMethodDesc(method: MethodNode) {
            val insnsToReplace = mutableMapOf<AbstractInsnNode, AbstractInsnNode>()
            val lvsToReplace = mutableMapOf<LocalVariableNode, LocalVariableNode>()

            for (node in method.instructions) {
                if (node is MethodInsnNode) {
                    if (node.desc.contains("L$TINY_TATER_TOKEN;") || node.owner.equals(TINY_TATER_TOKEN)) {
                        insnsToReplace[node] = MethodInsnNode(
                            node.opcode,
                            node.owner.replace(TINY_TATER_TOKEN, FLUID_TYPE_FACTORY_INTERFACE, node.itf),
                            node.name,
                            node.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;", node.itf)
                        )
                    }
                } else if (node is InvokeDynamicInsnNode) {
                    if (Opcodes.H_INVOKESTATIC != node.bsm.tag)
                        continue

                    if ("metafactory" != node.bsm.name)
                        continue

                    if (LAMBDA_CLASS_NAME != node.bsm.owner)
                        continue

                    if (LAMBDA_METHOD_DESCRIPTOR != node.bsm.desc)
                        continue

                    if (node.bsmArgs?.size == 3) {
                        if (node.bsmArgs[1] is Handle) {
                            val lambdaTarget = node.bsmArgs[1] as Handle
                            if (lambdaTarget.desc.contains("L$TINY_TATER_TOKEN;")) {
                                insnsToReplace[node] = InvokeDynamicInsnNode(
                                    node.name, node.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;"), node.bsm,
                                    *node.bsmArgs.toMutableList().apply {
                                        this[1] = Handle(lambdaTarget.tag, lambdaTarget.owner, lambdaTarget.name, lambdaTarget.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;"), lambdaTarget.isInterface)
                                    }.toTypedArray()
                                )
                            } else if (node.desc.contains("L$TINY_TATER_TOKEN;")) {
                                insnsToReplace[node] = InvokeDynamicInsnNode(
                                    node.name, node.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;"), node.bsm,
                                    *node.bsmArgs.toList().toTypedArray()
                                )
                            }
                        }
                    }
                }
            }

            for (node in method.localVariables) {
                if (node.desc.contains("L$TINY_TATER_TOKEN;")) {
                    lvsToReplace[node] = LocalVariableNode(node.name, node.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;"), node.signature?.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;"), node.start, node.end, node.index)
                }
            }

            for ((from, to) in insnsToReplace) {
                method.instructions.set(from, to)
            }

            for ((from, to) in lvsToReplace) {
                method.localVariables[method.localVariables.indexOf(from)] = to
            }

            method.desc = method.desc.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;")
            method.signature = method.signature?.replace("L$TINY_TATER_TOKEN;", "L$FLUID_TYPE_FACTORY_INTERFACE;")
        }
    }

    override fun checkActive(environment: MixinEnvironment?): Boolean {
        return true
    }

    override fun preApply(context: ITargetClassContext) {
        if (context.classInfo.name.contains("registrate") && FabricLoader.getInstance().isModLoaded("registrate-fabric")) {
            TargetClassContextExtension.tryAs(context) { ext ->
                for (mixinInfo in ext.mixins) {
                    // Don't process anything that isn't ours.
                    if (!mixinInfo.className.contains("kilt") || !mixinInfo.className.contains("registrate_fabric"))
                        continue

                    val mixinClassNode = mixinInfo.getClassNode(0)
                    var wasModified = false

                    for (method in mixinClassNode.methods) {
                        if (!method.desc.contains("L$TINY_TATER_TOKEN;")) continue

                        modifyMethodDesc(method)
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
    }

    override fun postApply(context: ITargetClassContext) {
    }

    override fun export(
        env: MixinEnvironment?,
        name: String?,
        force: Boolean,
        classNode: ClassNode?
    ) {
    }
}