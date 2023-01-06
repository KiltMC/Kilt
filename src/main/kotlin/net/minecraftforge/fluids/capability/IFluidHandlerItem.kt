package net.minecraftforge.fluids.capability

import net.minecraft.world.item.ItemStack

interface IFluidHandlerItem : IFluidHandler {
    fun getContainer(): ItemStack
}