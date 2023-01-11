package net.minecraftforge.common.crafting.conditions

import com.google.gson.JsonObject
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer
import net.minecraft.resources.ResourceLocation

interface IConditionSerializer<T : ICondition> {
    fun write(json: JsonObject, value: T)
    fun read(json: JsonObject): T
    val ID: ResourceLocation

    fun getJson(value: T): JsonObject {
        val json = JsonObject()
        write(json, value)
        json.addProperty("type", value.ID.toString())

        return json
    }
}