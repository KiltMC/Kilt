package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyVariableNode
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.injection.Inject
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

        return annotationNode
    }
}