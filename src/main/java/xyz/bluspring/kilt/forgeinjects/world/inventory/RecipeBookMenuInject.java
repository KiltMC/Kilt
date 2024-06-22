// TRACKED HASH: 5e2075b943a16e07d906de887d9ca451cc35cc0b
package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    @Environment(EnvType.CLIENT)
    @Override
    public List<RecipeBookCategories> getRecipeBookCategories() {
        return RecipeBookCategories.getCategories(this.getRecipeBookType());
    }
}