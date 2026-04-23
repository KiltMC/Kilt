package xyz.bluspring.kilt.injects.world.item.crafting;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

@Mixin(SmithingTrimRecipe.class)
public abstract class SmithingTrimRecipeInject {
    @ModifyArg(method = "isIncomplete", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private <T extends Ingredient> Predicate<? super T> kilt$checkHasNoItems(Predicate<? super T> predicate) {
        return ingredient -> predicate.test(ingredient) || ingredient.hasNoItems();
    }
}
