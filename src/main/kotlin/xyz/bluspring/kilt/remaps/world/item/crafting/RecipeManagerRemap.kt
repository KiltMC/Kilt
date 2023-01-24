package xyz.bluspring.kilt.remaps.world.item.crafting

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.common.extensions.IForgeRecipeSerializer

open class RecipeManagerRemap : RecipeManager() {
    companion object {
        @JvmStatic
        fun fromJson(location: ResourceLocation, json: JsonObject, context: ICondition.IContext): Recipe<*> {
            val string = GsonHelper.getAsString(json, "type")
            return ((Registry.RECIPE_SERIALIZER.getOptional(ResourceLocation(string)).orElseThrow {
                JsonSyntaxException(
                    "Invalid or unsupported recipe type '$string'"
                )
            } as RecipeSerializer<*>) as IForgeRecipeSerializer<*>).fromJson(location, json, context)
        }
    }
}