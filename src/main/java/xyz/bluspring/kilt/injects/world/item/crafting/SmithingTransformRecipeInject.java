package xyz.bluspring.kilt.injects.world.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformRecipeInject {
    @ModifyArg(method = "isIncomplete", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private <T> Predicate<? super T> kilt$tryCheckHasNoElements(Predicate<? super T> predicate) {
        return value -> predicate.test(value) || CommonHooks.hasNoElements((Ingredient) value);
    }
}
