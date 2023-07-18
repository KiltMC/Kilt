package xyz.bluspring.kilt.workarounds

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions

class ForgeFluidRenderHandler : FluidRenderHandler {
    override fun getFluidSprites(
        view: BlockAndTintGetter?,
        pos: BlockPos?,
        state: FluidState?
    ): Array<TextureAtlasSprite?> {
        return ForgeHooksClientWorkaround.getFluidSprites(view, pos, state)
    }

    override fun getFluidColor(view: BlockAndTintGetter?, pos: BlockPos?, state: FluidState?): Int {
        return IClientFluidTypeExtensions.of(state).getTintColor(state, view, pos)
    }
}