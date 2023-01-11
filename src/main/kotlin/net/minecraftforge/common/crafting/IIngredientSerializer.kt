package net.minecraftforge.common.crafting

import com.google.gson.JsonObject
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.crafting.Ingredient

interface IIngredientSerializer<T : Ingredient> : IngredientDeserializer {
    fun parse(buffer: FriendlyByteBuf): T
    fun parse(json: JsonObject): T
    fun write(buffer: FriendlyByteBuf, ingredient: T)

    override fun fromJson(json: JsonObject): T {
        return parse(json)
    }

    override fun fromNetwork(buffer: FriendlyByteBuf): T {
        return parse(buffer)
    }
}