package net.minecraftforge.fluids.capability

import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent

@Deprecated("Forge 1.19.2 has deprecated this, remove this later")
object CapabilityFluidHandler {
    @JvmStatic
    val FLUID_HANDLER_CAPABILITY = ForgeCapabilities.FLUID_HANDLER
    @JvmStatic
    val FLUID_HANDLER_ITEM_CAPABILITY = ForgeCapabilities.FLUID_HANDLER_ITEM

    @JvmStatic
    fun register(event: RegisterCapabilitiesEvent) {}
}