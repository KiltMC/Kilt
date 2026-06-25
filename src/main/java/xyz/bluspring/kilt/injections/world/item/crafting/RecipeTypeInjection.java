package xyz.bluspring.kilt.injections.world.item.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface RecipeTypeInjection {
    static <T extends Recipe<?>> RecipeType<T> simple(Identifier name) {
        var serialized = name.toString();

        return new RecipeType<T>() {
            @Override
            public String toString() {
                return serialized;
            }
        };
    }
}
