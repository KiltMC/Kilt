// TRACKED HASH: 12b17cf5ecf56046e0c8f2d76638acdc60c56dfb
package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.ShapedRecipePatternStorage;
import xyz.bluspring.kilt.injections.world.item.crafting.IngredientInjection;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeInject {
    @Shadow @Final private NonNullList<Ingredient> ingredients;
    private boolean isSimple;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void kilt$checkIfIsSimple(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, CallbackInfo ci) {
        this.isSimple = ingredients.stream().allMatch(IngredientInjection::isSimple);
    }

	@Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "RETURN", ordinal = 1))
	private void findNonSimpleMatches(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir, @Share("inputs") LocalRef<List<ItemStack>> inputsRef) {
        if (!isSimple) {
            List<ItemStack> nonEmptyItems = new ArrayList<>(input.ingredientCount());
            for (ItemStack item : input.items())
                if (!item.isEmpty())
                    nonEmptyItems.add(item);
            cir.setReturnValue(RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null);
        }
	}

    @Mixin(ShapelessRecipe.Serializer.class)
    public static class SerializerInject {
        @ModifyExpressionValue(method = "method_53757", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;isEmpty()Z"))
        private static boolean disableIsEmptyCheck(boolean original) {
            return false;
        }

        @ModifyConstant(method = "method_53760", constant = @Constant(intValue = 9))
        private static int modifyRecipeSize(int constant) {
            return ShapedRecipePatternStorage.getMaxHeight() * ShapedRecipePatternStorage.getMaxWidth();
        }
    }
}