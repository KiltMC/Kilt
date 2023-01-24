package xyz.bluspring.kilt.remaps.world.item.crafting

import net.minecraft.world.item.crafting.Ingredient
import java.util.*
import java.util.stream.Stream

open class IngredientRemap(stream: Stream<out Ingredient.Value>) : Ingredient(stream) {
    companion object {
        @JvmStatic
        fun merge(parts: Collection<Ingredient>): Ingredient {
            return Ingredient.fromValues(
                parts.stream().flatMap {
                    Arrays.stream(it.values)
                }
            )
        }
    }
}