package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import xyz.bluspring.kilt.mixin.RecipeBookCategoriesAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface RecipeBookCategoriesInjection {
    static RecipeBookCategories create(String name, ItemStack... icons) {
        return EnumUtils.addEnumToClass(
                RecipeBookCategories.class,
                RecipeBookCategoriesAccessor.getValues(),
                name,
                (size) -> RecipeBookCategoriesAccessor.createRecipeBookCategories(name, size, icons),
                (values) -> RecipeBookCategoriesAccessor.setValues(values.toArray(new RecipeBookCategories[0]))
        );
    }
}
