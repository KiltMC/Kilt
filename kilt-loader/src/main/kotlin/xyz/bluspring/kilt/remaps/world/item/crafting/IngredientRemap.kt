package xyz.bluspring.kilt.remaps.world.item.crafting

import net.minecraft.world.item.crafting.Ingredient
import java.util.*

object IngredientRemap {
    @JvmStatic
    fun merge(parts: Collection<Ingredient>): Ingredient {
        return Ingredient.fromValues(
            parts.stream().flatMap {
                Arrays.stream(it.values)
            }
        )
    }
}