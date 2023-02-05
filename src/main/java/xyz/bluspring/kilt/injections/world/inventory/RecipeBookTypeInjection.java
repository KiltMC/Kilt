package xyz.bluspring.kilt.injections.world.inventory;

import net.minecraft.world.inventory.RecipeBookType;
import xyz.bluspring.kilt.mixin.RecipeBookTypeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface RecipeBookTypeInjection {
    static RecipeBookType create(String name) {
        return EnumUtils.addEnumToClass(
                RecipeBookType.class, RecipeBookTypeAccessor.getValues(),
                name, (size) -> RecipeBookTypeAccessor.createRecipeBookType(name, size),
                (values) -> RecipeBookTypeAccessor.setValues(values.toArray(new RecipeBookType[0]))
        );
    }
}
