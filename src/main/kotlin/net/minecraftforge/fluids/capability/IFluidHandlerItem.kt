package net.minecraftforge.fluids.capability

import net.minecraft.world.item.ItemStack

interface IFluidHandlerItem : IFluidHandler {
    val container: ItemStack
}