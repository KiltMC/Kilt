package xyz.bluspring.kilt.injections.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface RecipeProviderInjection {
    @Nullable
    CompletableFuture<?> buildAdvancement(CachedOutput output, HolderLookup.Provider provider, AdvancementHolder holder, ICondition... conditions);
}
