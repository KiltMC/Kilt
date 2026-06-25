package xyz.bluspring.kilt.injections.world.inventory;

import java.util.List;

import net.minecraft.world.item.crafting.RecipeBookCategories;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface RecipeBookMenuInjection {
    @Environment(EnvType.CLIENT)
    default List<RecipeBookCategories> getRecipeBookCategories() {
        throw new IllegalStateException();
    }
}
