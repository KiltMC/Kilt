package xyz.bluspring.kilt.workarounds

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.minecraft.client.renderer.block.BlockAndTintGetter
import net.minecraft.client.renderer.block.FluidRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer

class NeoForgeFluidRenderHandler(val wrapped: CustomFluidRenderer) : FluidRenderHandler {
    override fun renderFluid(fluidRenderer: FluidRenderer, pos: BlockPos, level: BlockAndTintGetter, output: FluidRenderer.Output, blockState: BlockState, fluidState: FluidState) {
        this.wrapped.renderFluid(fluidRenderer, fluidState, level, pos, output, blockState)
    }
}
