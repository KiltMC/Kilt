package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyArgNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyVariableNode
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyArg
import org.spongepowered.asm.mixin.injection.ModifyVariable

class KiltAnnotationAdjuster : MixinAnnotationAdjuster {
    override fun adjust(
        targetClassNames: List<String?>?,
        mixinClassName: String,
        handlerNode: MethodNode,
        annotationNode: AdjustableAnnotationNode
    ): AdjustableAnnotationNode? {
        if (mixinClassName == "org.violetmoon.quark.mixin.mixins.client.ContainerScreenMixin" && annotationNode.`is`(ModifyVariable::class.java)) {
            val variableNode = annotationNode.`as`(AdjustableModifyVariableNode::class.java)
            if (variableNode.method[0].equals("mouseClicked(DDI)Z"))
                return variableNode.withIndex({ 14 })
            else if (variableNode.method[0].equals("mouseReleased(DDI)Z"))
                return variableNode.withIndex({ 11 })
        }

        if (mixinClassName == "yesman.epicfight.mixin.common.MixinLivingEntity" && annotationNode.`is`(Inject::class.java) && handlerNode.name == "epicfight_constructor") {
            val injectNode = annotationNode.`as`(AdjustableInjectNode::class.java)
            // seriously wtf why
            injectNode.cancellable = false
        }

        // Optifine based moment
        if (mixinClassName == "org.violetmoon.quark.mixin.mixins.client.HumanoidArmorLayerMixin" && annotationNode.`is`(ModifyExpressionValue::class.java) && handlerNode.name == "quark\$getArmorGlint") {
            return null
        }

        // Replaces the at renderModel forge added method with the vanilla method since they aren't using anything from the patch
        if (mixinClassName == "com.gregtechceu.gtceu.core.mixins.client.HumanoidArmorLayerMixin" && annotationNode.`is`(
                ModifyArg::class.java) && handlerNode.name.startsWith("gtceu\$modifyArmorTint")) {
            val modifyArgNode = annotationNode.`as`(AdjustableModifyArgNode::class.java)
            return modifyArgNode.withAt { at ->
                at.withTarget {
                    "Lnet/minecraft/class_970;method_23192(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1738;Lnet/minecraft/class_572;ZFFFLjava/lang/String;)V"
                }.withRemap { true }
                at
            }
        }

        // Make Create Fabric not mixin into instances of `getFluidType` other than Porting Lib's
        if (mixinClassName == "com.simibubi.create.foundation.mixin.fabric.FluidMixin" &&
            annotationNode.`is`(Inject::class.java) && handlerNode.name == "getFluidType")
        {
            val injectNode = annotationNode.`as`(AdjustableInjectNode::class.java)
            injectNode.method[0] = "getFluidType()Lio/github/fabricators_of_create/porting_lib/fluids/mixin/FluidType;"
        }

        return annotationNode
    }
}