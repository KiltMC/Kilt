package xyz.bluspring.kilt.injections.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface RecipeProviderInjection {
    default void kilt$saveAdvancement(CachedOutput output, JsonObject advancementJson, Path path) {
        RecipeProvider.saveAdvancement(output, advancementJson, path);
    }

    default void kilt$buildCraftingRecipes(Consumer<FinishedRecipe> finishedRecipeConsumer) {
        RecipeProvider.buildCraftingRecipes(finishedRecipeConsumer);
    }
}
