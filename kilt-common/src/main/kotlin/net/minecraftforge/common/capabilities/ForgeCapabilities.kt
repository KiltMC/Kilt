package net.minecraftforge.common.capabilities

import energy.IEnergyStorage
import fluids.capability.IFluidHandler
import fluids.capability.IFluidHandlerItem
import net.minecraftforge.items.IItemHandler

object ForgeCapabilities {
    @JvmField
    val ENERGY: Capability<energy.IEnergyStorage> =
        CapabilityManager.get(object : CapabilityToken<energy.IEnergyStorage>() {})

    @JvmField
    val FLUID_HANDLER: Capability<fluids.capability.IFluidHandler> =
        CapabilityManager.get(object : CapabilityToken<fluids.capability.IFluidHandler>() {})

    @JvmField
    val FLUID_HANDLER_ITEM: Capability<fluids.capability.IFluidHandlerItem> =
        CapabilityManager.get(object : CapabilityToken<fluids.capability.IFluidHandlerItem>() {})

    @JvmField
    val ITEM_HANDLER: Capability<IItemHandler> = CapabilityManager.get(object : CapabilityToken<IItemHandler>() {})
}