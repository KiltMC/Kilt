// TRACKED HASH: cdaa6bc13984c0e47e37365d1be1e2f32b4453e0
package xyz.bluspring.kilt.injects.client.gui.screens.recipebook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.inventory.RecipeBookMenuInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentInject {
    @Shadow protected RecipeBookMenu<?, ?> menu;

    @WrapOperation(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/RecipeBookCategories;getCategories(Lnet/minecraft/world/inventory/RecipeBookType;)Ljava/util/List;"))
    public List<RecipeBookCategories> kilt$getMenuRecipeCategories(RecipeBookType recipeBookType, Operation<List<RecipeBookCategories>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.menu.getClass(), RecipeBookMenu.class, "getRecipeBookCategories")) {
            return ((RecipeBookMenuInjection) this.menu).getRecipeBookCategories();
        }

        return original.call(recipeBookType);
    }
}