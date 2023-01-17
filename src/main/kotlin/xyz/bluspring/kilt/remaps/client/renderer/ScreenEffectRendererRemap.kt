package xyz.bluspring.kilt.remaps.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.kilt.mixin.ScreenEffectRendererAccessor
import java.util.concurrent.atomic.AtomicReference

object ScreenEffectRendererRemap {
    @JvmField
    val currentTexture = AtomicReference<ResourceLocation>()

    @JvmStatic
    fun renderFluid(mc: Minecraft, poseStack: PoseStack, texture: ResourceLocation) {
        currentTexture.set(texture)
        ScreenEffectRendererAccessor.callRenderWater(mc, poseStack)
        currentTexture.set(ScreenEffectRendererAccessor.getUnderwaterLocation())
    }
}