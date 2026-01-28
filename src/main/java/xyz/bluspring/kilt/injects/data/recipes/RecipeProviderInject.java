// TRACKED HASH: ea137127081f8e3b35dd94c07fdca929038853ea
package xyz.bluspring.kilt.injects.data.recipes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.advancements.AdvancementInjection;
import xyz.bluspring.kilt.injections.data.recipes.RecipeProviderInjection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(RecipeProvider.class)
public class RecipeProviderInject implements RecipeProviderInjection {
    @Shadow public PackOutput.PathProvider advancementPathProvider;

    @Override
    public @Nullable CompletableFuture<?> buildAdvancement(CachedOutput output, HolderLookup.Provider provider, AdvancementHolder holder, ICondition... conditions) {
        return DataProvider.saveStable(output, provider, AdvancementInjection.CONDITIONAL_CODEC, Optional.of(new WithConditions<>(holder.value(), conditions)), this.advancementPathProvider.json(holder.id()));
    }
}