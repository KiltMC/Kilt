package xyz.bluspring.kilt.compat.create.extensions

import net.minecraftforge.fluids.FluidType
import java.util.function.Supplier

interface SimpleFlowableFluidPropertiesExtension {
    fun `kilt$getFluidType`(): Supplier<out FluidType>
    fun `kilt$setFluidType`(fluidType: Supplier<out FluidType>)
}