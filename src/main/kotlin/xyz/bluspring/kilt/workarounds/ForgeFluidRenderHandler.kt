package xyz.bluspring.kilt.workarounds

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.util.FastColor
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions

class ForgeFluidRenderHandler : FluidRenderHandler {
    override fun getFluidSprites(
        view: BlockAndTintGetter?,
        pos: BlockPos?,
        state: FluidState?
    ): Array<TextureAtlasSprite?> {
        return ForgeHooksClient.getFluidSprites(view, pos, state).filterNotNull().toTypedArray()
    }

    override fun getFluidColor(view: BlockAndTintGetter?, pos: BlockPos?, state: FluidState?): Int {
        val bgr = IClientFluidTypeExtensions.of(state).getTintColor(state, view, pos)
        val r = FastColor.ABGR32.red(bgr)
        val g = FastColor.ABGR32.green(bgr)
        val b = FastColor.ABGR32.blue(bgr)
        val a = FastColor.ABGR32.alpha(bgr)
        return FastColor.ARGB32.color(a, r, g, b)
    }
}