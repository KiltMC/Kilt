package xyz.bluspring.kilt.injections.world.item.crafting;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeMapInjection {
    default void order(Object2IntMap<ResourceKey<Recipe<?>>> recipePriorities) {
        throw KiltHelper.createMixinException(RecipeManagerInjection.class, "order");
    }
}
