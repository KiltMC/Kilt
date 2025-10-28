// TRACKED HASH: 6b717e608f1a84947c867222d601c601a3b66507
package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeInject {
    @ModifyReturnValue(method = "method_31585", at = @At("RETURN"))
    private static boolean checkHasNoItems(boolean original, Ingredient ingredient) {
        if (!original) {
            return ingredient.hasNoItems();
        }
        return true;
    }
}