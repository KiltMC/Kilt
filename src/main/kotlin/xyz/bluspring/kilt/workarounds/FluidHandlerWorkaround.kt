package xyz.bluspring.kilt.workarounds

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.minecraftforge.fluids.FluidType

object FluidHandlerWorkaround {
    private val forgeFluidRenderHandler = ForgeFluidRenderHandler()

    fun getFluidRenderHandler(fluidType: FluidType): FluidRenderHandler {
        return forgeFluidRenderHandler
    }
}