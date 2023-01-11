package net.minecraftforge.event.brewing

import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

open class PotionBrewEvent protected constructor(stacks: List<ItemStack>) : Event() {
    private val stacks = stacks.toMutableList()

    fun getItem(index: Int): ItemStack {
        if (index < 0 || index >= stacks.size)
            return ItemStack.EMPTY

        return stacks[index]
    }

    fun setItem(index: Int, stack: ItemStack) {
        if (index < stacks.size)
            stacks[index] = stack
    }

    val length: Int
        get() = stacks.size

    @Cancelable
    class Pre(stacks: List<ItemStack>) : PotionBrewEvent(stacks)

    @Cancelable
    class Post(stacks: List<ItemStack>) : PotionBrewEvent(stacks)
}