package xyz.bluspring.kilt.remaps.world.item.crafting

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

interface RecipeTypeRemap<T : Recipe<*>> : RecipeType<T> {
    companion object {
        @JvmStatic
        fun <T : Recipe<*>> simple(name: ResourceLocation): RecipeType<T> {
            val serialized = name.toString()

            return object : RecipeType<T> {
                override fun toString(): String {
                    return serialized
                }
            }
        }
    }
}