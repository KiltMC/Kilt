package net.minecraftforge.event.furnace

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

@Cancelable
class FurnaceFuelBurnTimeEvent(val itemStack: ItemStack, var burnTime: Int, val recipeType: RecipeType<*>?) : Event() {
}