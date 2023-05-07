package xyz.bluspring.kilt.forgeinjects.client.gui.screens.recipebook;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.world.inventory.RecipeBookMenuInjection;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentInject {
    @Shadow protected RecipeBookMenu<?> menu;

    @Redirect(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/RecipeBookCategories;getCategories(Lnet/minecraft/world/inventory/RecipeBookType;)Ljava/util/List;"))
    public List<RecipeBookCategories> kilt$getMenuRecipeCategories(RecipeBookType recipeBookType) {
        return ((RecipeBookMenuInjection) this.menu).getRecipeBookCategories();
    }
}
