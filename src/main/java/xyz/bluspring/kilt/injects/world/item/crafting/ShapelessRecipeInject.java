// TRACKED HASH: 12b17cf5ecf56046e0c8f2d76638acdc60c56dfb
package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.item.crafting.IngredientInjection;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeInject {
    @Shadow @Final private NonNullList<Ingredient> ingredients;
    private boolean isSimple;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$checkIfIsSimple(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, CallbackInfo ci) {
        this.isSimple = ingredients.stream().allMatch(IngredientInjection::isSimple);
    }

	@Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"))
	private void kilt$initInputsList(CraftingContainer inv, Level level, CallbackInfoReturnable<Boolean> cir, @Share("inputs") LocalRef<List<ItemStack>> inputsRef) {
		inputsRef.set(new ArrayList<>());
	}

    @WrapOperation(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;accountStack(Lnet/minecraft/world/item/ItemStack;I)V"))
	private void kilt$addToInputsIfSimple(StackedContents instance, ItemStack stack, int amount, Operation<Void> original, @Share("inputs") LocalRef<List<ItemStack>> inputsRef) {
		if (isSimple)
			original.call(instance, stack, amount);
		else
			inputsRef.get().add(stack);
	}

	@WrapOperation(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;canCraft(Lnet/minecraft/world/item/crafting/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
	private boolean kilt$tryFindMatchesIfSimple(StackedContents instance, Recipe<?> recipe, @Nullable IntList stackingIndexList, Operation<Boolean> original, @Share("inputs") LocalRef<List<ItemStack>> inputsRef) {
		return isSimple ? original.call(instance, recipe, stackingIndexList) : RecipeMatcher.findMatches(inputsRef.get(), this.ingredients) != null;
	}
}