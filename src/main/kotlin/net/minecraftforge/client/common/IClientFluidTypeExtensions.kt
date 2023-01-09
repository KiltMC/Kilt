package net.minecraftforge.client.common

import com.mojang.blaze3d.shaders.FogShape
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.client.renderer.ScreenEffectRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidType
import java.util.*
import java.util.stream.Stream

interface IClientFluidTypeExtensions {
    val tintColor: Int
        get() = 0xFFFFFFFF.toInt()

    val stillTexture: ResourceLocation?
        get() = null

    val flowingTexture: ResourceLocation?
        get() = null

    val overlayTexture: ResourceLocation?
        get() = null

    val textures: Stream<ResourceLocation>
        get() = Stream.of(stillTexture, flowingTexture, overlayTexture)
            .filter { it != null }.map { it!! }

    fun getRenderOverlayTexture(mc: Minecraft): ResourceLocation? {
        return null
    }

    fun renderOverlay(mc: Minecraft, poseStack: PoseStack) {
        val texture = getRenderOverlayTexture(mc)
        if (texture != null)
            ScreenEffectRenderer.renderFluid(mc, poseStack, texture)
    }

    fun modifyFogColor(camera: Camera, partialTick: Float, level: ClientLevel, renderDistance: Int, darkenWorldAmount: Float, fluidFogColor: Vector3f): Vector3f {
        return fluidFogColor
    }

    fun modifyFogRender(camera: Camera, mode: FogRenderer.FogMode, renderDistance: Float, partialTick: Float, nearDistance: Float, farDistance: Float, shape: FogShape) {
    }

    fun getStillTexture(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): ResourceLocation? {
        return stillTexture
    }

    fun getFlowingTexture(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): ResourceLocation? {
        return flowingTexture
    }

    fun getOverlayTexture(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): ResourceLocation? {
        return overlayTexture
    }

    fun getTintColor(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
        return tintColor
    }

    fun getTintColor(stack: FluidStack): Int {
        return tintColor
    }

    fun getStillTexture(stack: FluidStack): ResourceLocation? {
        return stillTexture
    }

    fun getFlowingTexture(stack: FluidStack): ResourceLocation? {
        return flowingTexture
    }

    fun getOverlayTexture(stack: FluidStack): ResourceLocation? {
        return overlayTexture
    }

    companion object {
        @JvmField
        val DEFAULT = object : IClientFluidTypeExtensions {}

        @JvmStatic
        fun of(state: FluidState): IClientFluidTypeExtensions {
            return of(state.fluidType)
        }

        @JvmStatic
        fun of(fluid: Fluid): IClientFluidTypeExtensions {
            return of(fluid.fluidType)
        }

        @JvmStatic
        fun of(type: FluidType): IClientFluidTypeExtensions {
            return if (type.renderPropertiesInternal is IClientFluidTypeExtensions)
                type.renderPropertiesInternal as IClientFluidTypeExtensions
            else DEFAULT
        }
    }
}