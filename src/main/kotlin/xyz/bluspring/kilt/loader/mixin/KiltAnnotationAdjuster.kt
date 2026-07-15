package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyArgNode
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyVariableNode
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyArg
import org.spongepowered.asm.mixin.injection.ModifyVariable
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class KiltAnnotationAdjuster : MixinAnnotationAdjuster {
    override fun adjust(
        targetClassNames: List<String?>?,
        mixinClassName: String,
        handlerNode: MethodNode,
        annotationNode: AdjustableAnnotationNode
    ): AdjustableAnnotationNode? {
        if (mixinClassName == "org.violetmoon.quark.mixin.mixins.client.ContainerScreenMixin" && annotationNode.`is`(ModifyVariable::class.java)) {
            val variableNode = annotationNode.`as`(AdjustableModifyVariableNode::class.java)
            val mouseClickedName = KiltRemapper.enhancedRemapper.mapMethodName("net/minecraft/client/gui/screens/inventory/AbstractContainerScreen", "mouseClicked", "(DDI)Z")
            val mouseReleasedName = KiltRemapper.enhancedRemapper.mapMethodName("net/minecraft/client/gui/screens/inventory/AbstractContainerScreen", "mouseReleased", "(DDI)Z")
            if (variableNode.method[0].equals("$mouseClickedName(DDI)Z"))
                return variableNode.withIndex({ 14 })
            else if (variableNode.method[0].equals("$mouseReleasedName(DDI)Z"))
                return variableNode.withIndex({ 11 })
        }

        if (mixinClassName == "yesman.epicfight.mixin.common.MixinLivingEntity" && annotationNode.`is`(Inject::class.java) && handlerNode.name == "epicfight_constructor") {
            val injectNode = annotationNode.`as`(AdjustableInjectNode::class.java)
            // seriously wtf why
            injectNode.cancellable = false
        }

        // otherwise I legitimately cannot fucking load into my dev env.
        if (FabricLoader.getInstance().isDevelopmentEnvironment && FabricLoader.getInstance().isModLoaded("iris")
            // only happens in 1.8.14+
            && FabricLoader.getInstance().getModContainer("iris").orElseThrow().metadata.version >= Version.parse("1.8.14-")
        ) {
            if (mixinClassName.endsWith(".entity_render_context.MixinHumanoidArmorLayer") && handlerNode.name == "changeId") {
                val injectNode = annotationNode.`as`(AdjustableInjectNode::class.java)
                injectNode.method = listOf("Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V")
            }

            if (mixinClassName.endsWith(".MixinLevelRenderer") && handlerNode.name == $$"iris$writeRainAndSnowToDepthBuffer") {
                val modifyArgNode = annotationNode.`as`(AdjustableModifyArgNode::class.java)
                modifyArgNode.method = listOf("Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V")
            }
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

        // LittleTiles is exploding because of Fabric mixin stuff
        if (mixinClassName == "team.creative.littletiles.mixin.common.entity.EntityMixin") {
            return null
        }

        return annotationNode
    }
}
