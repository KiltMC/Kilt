package xyz.bluspring.kilt.injections.world.inventory;

import net.minecraft.client.RecipeBookCategories;

import java.util.List;

public interface RecipeBookMenuInjection {
    default List<RecipeBookCategories> getRecipeBookCategories() {
        throw new IllegalStateException();
    }
}
