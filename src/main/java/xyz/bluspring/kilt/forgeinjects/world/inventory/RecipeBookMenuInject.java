package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.inventory.RecipeBookMenuInjection;

import java.util.List;

@Mixin(RecipeBookMenu.class)
public abstract class RecipeBookMenuInject implements RecipeBookMenuInjection {
    @Shadow public abstract RecipeBookType getRecipeBookType();

    @Override
    public List<RecipeBookCategories> getRecipeBookCategories() {
        return RecipeBookCategories.getCategories(this.getRecipeBookType());
    }
}
