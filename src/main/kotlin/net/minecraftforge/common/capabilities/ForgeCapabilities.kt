package net.minecraftforge.common.capabilities

import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandlerItem
import net.minecraftforge.items.IItemHandler

object ForgeCapabilities {
    @JvmStatic
    val ENERGY: Capability<IEnergyStorage> = CapabilityManager.get(object : CapabilityToken<IEnergyStorage>() {})

    @JvmStatic
    val FLUID_HANDLER: Capability<IFluidHandler> = CapabilityManager.get(object : CapabilityToken<IFluidHandler>() {})

    @JvmStatic
    val FLUID_HANDLER_ITEM: Capability<IFluidHandlerItem> = CapabilityManager.get(object : CapabilityToken<IFluidHandlerItem>() {})

    @JvmStatic
    val ITEM_HANDLER: Capability<IItemHandler> = CapabilityManager.get(object : CapabilityToken<IItemHandler>() {})
}