package xyz.bluspring.kilt.injections.data.recipes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.concurrent.CompletableFuture;

@FabricInjectedInterface(RecipeProvider.class)
public interface RecipeProviderInjection {
    @Nullable
    CompletableFuture<?> buildAdvancement(CachedOutput output, HolderLookup.Provider provider, AdvancementHolder holder, ICondition... conditions);
}
