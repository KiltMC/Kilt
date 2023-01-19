package xyz.bluspring.kilt.remaps.world.item.crafting

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import common.crafting.conditions.ICondition
import common.extensions.IForgeRecipeSerializer

object RecipeManagerRemap {
    @JvmStatic
    fun fromJson(location: ResourceLocation, json: JsonObject, context: common.crafting.conditions.ICondition.IContext): Recipe<*> {
        val string = GsonHelper.getAsString(json, "type")
        return ((Registry.RECIPE_SERIALIZER.getOptional(ResourceLocation(string)).orElseThrow {
            JsonSyntaxException(
                "Invalid or unsupported recipe type '$string'"
            )
        } as RecipeSerializer<*>) as common.extensions.IForgeRecipeSerializer<*>).fromJson(location, json, context)
    }
}