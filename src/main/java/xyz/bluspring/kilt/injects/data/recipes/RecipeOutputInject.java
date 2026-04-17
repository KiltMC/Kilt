package xyz.bluspring.kilt.injects.data.recipes;

import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IRecipeOutputExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@Mixin(RecipeOutput.class)
public interface RecipeOutputInject extends IRecipeOutputExtension {
    @Shadow
    void accept(ResourceLocation location, Recipe<?> recipe, @Nullable AdvancementHolder advancement);

    // Kilt: do we need this?

    @Override
    default void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        accept(id, recipe, advancement);
    }
}
