package net.minecraftforge.common.capabilities

import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandlerItem
import net.minecraftforge.items.IItemHandler

object ForgeCapabilities {
    @JvmField
    val ENERGY: Capability<IEnergyStorage> = CapabilityManager.get(object : CapabilityToken<IEnergyStorage>() {})

    @JvmField
    val FLUID_HANDLER: Capability<IFluidHandler> = CapabilityManager.get(object : CapabilityToken<IFluidHandler>() {})

    @JvmField
    val FLUID_HANDLER_ITEM: Capability<IFluidHandlerItem> = CapabilityManager.get(object : CapabilityToken<IFluidHandlerItem>() {})

    @JvmField
    val ITEM_HANDLER: Capability<IItemHandler> = CapabilityManager.get(object : CapabilityToken<IItemHandler>() {})
}