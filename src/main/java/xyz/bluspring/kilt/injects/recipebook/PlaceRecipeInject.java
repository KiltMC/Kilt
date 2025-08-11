package xyz.bluspring.kilt.injects.recipebook;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(PlaceRecipe.class)
public interface PlaceRecipeInject<T> {
    @Inject(method = "placeRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/ShapedRecipe;getHeight()I", shift = At.Shift.BY, by = 2))
    private void kilt$tryUseForgeShapedRecipeCheck(int width, int height, int outputSlot, Recipe<?> recipe, Iterator<T> ingredients, int maxAmount, CallbackInfo ci, @Local(ordinal = 4) LocalIntRef recipeWidth, @Local(ordinal = 5) LocalIntRef recipeHeight) {
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            recipeWidth.set(shapedRecipe.getRecipeWidth());
            recipeHeight.set(shapedRecipe.getRecipeHeight());
        }
    }
}
