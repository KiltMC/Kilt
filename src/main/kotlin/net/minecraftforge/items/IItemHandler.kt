package net.minecraftforge.items

import net.minecraft.world.item.ItemStack

interface IItemHandler {
    val slots: Int
    fun getStackInSlot(slot: Int): ItemStack
    fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack
    fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack
    fun getSlotLimit(slot: Int): Int
    fun isItemValid(slot: Int, stack: ItemStack): Boolean
}