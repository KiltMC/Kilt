package xyz.bluspring.kilt.injections.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface RecipeTypeInjection {
    static <T extends Recipe<?>> RecipeType<T> simple(ResourceLocation name) {
        var serialized = name.toString();

        return new RecipeType<T>() {
            @Override
            public String toString() {
                return serialized;
            }
        };
    }
}
