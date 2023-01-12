package net.minecraftforge.items

import net.minecraft.world.item.ItemStack

interface IItemHandlerModifiable : IItemHandler {
    fun setStackInSlot(slot: Int, stack: ItemStack)
}