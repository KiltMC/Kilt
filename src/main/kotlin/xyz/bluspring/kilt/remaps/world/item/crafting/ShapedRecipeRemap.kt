package xyz.bluspring.kilt.remaps.world.item.crafting

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe

open class ShapedRecipeRemap(resourceLocation: ResourceLocation, string: String, i: Int, j: Int, nonNullList: NonNullList<Ingredient>, itemStack: ItemStack)
    : ShapedRecipe(resourceLocation, string, i, j, nonNullList, itemStack) {
    companion object {
        @JvmField
        var MAX_WIDTH = 3
        @JvmField
        var MAX_HEIGHT = 3

        @JvmStatic
        fun setCraftingSize(width: Int, height: Int) {
            if (MAX_WIDTH < width)
                MAX_WIDTH = width

            if (MAX_HEIGHT < height)
                MAX_HEIGHT = height
        }
    }
}