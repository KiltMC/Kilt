package xyz.bluspring.kilt.injections.world.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.RecipeBookCategories;

import java.util.List;

public interface RecipeBookMenuInjection {
    @Environment(EnvType.CLIENT)
    default List<RecipeBookCategories> getRecipeBookCategories() {
        throw new IllegalStateException();
    }
}
