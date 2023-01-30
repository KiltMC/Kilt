package xyz.bluspring.kilt.injections.item.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.extensions.IForgeRecipeSerializer;

public interface RecipeManagerInjection {
    static Recipe<?> fromJson(ResourceLocation location, JsonObject json, ICondition.IContext context) {
        var string = GsonHelper.getAsString(json, "type");

        return ((IForgeRecipeSerializer<?>) Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(string))
                .orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'"))
        ).fromJson(location, json, context);
    }
}
