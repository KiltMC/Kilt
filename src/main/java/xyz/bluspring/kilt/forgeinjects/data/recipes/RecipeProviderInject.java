package xyz.bluspring.kilt.forgeinjects.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.data.recipes.RecipeProviderInjection;

import java.util.concurrent.CompletableFuture;

@Mixin(RecipeProvider.class)
public class RecipeProviderInject implements RecipeProviderInjection {
    @Shadow public PackOutput.PathProvider advancementPathProvider;

    @Override
    public @Nullable CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
        return DataProvider.saveStable(output, advancementJson, this.advancementPathProvider.json(finishedRecipe.getAdvancementId()));
    }
}
