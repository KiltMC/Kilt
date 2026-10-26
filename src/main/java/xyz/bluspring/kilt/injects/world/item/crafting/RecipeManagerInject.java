package xyz.bluspring.kilt.injects.world.item.crafting;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.item.crafting.RecipeManagerInjection;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerInject implements RecipeManagerInjection {
    @Shadow private RecipeMap recipes;

    @Unique private Object2IntMap<ResourceKey<Recipe<?>>> recipePriorities = Object2IntMaps.emptyMap();

    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void kilt$reorderRecipesByPriority(CallbackInfo ci) {
        this.recipes.order(this.recipePriorities);
    }

    @Override
    public RecipeMap recipeMap() {
        return this.recipes;
    }

    @Override
    public void setPriorityMap(Object2IntMap<ResourceKey<Recipe<?>>> recipePriorities) {
        this.recipePriorities = recipePriorities;
    }
}
