package xyz.bluspring.kilt.injections.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface RecipeProviderInjection {
    @Nullable
    CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson);
}
