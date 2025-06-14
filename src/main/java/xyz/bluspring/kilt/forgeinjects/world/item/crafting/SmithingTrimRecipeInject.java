package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(SmithingTrimRecipe.class)
public abstract class SmithingTrimRecipeInject {
    @ModifyArg(method = "isIncomplete", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private <T> Predicate<? super T> kilt$tryCheckHasNoElements(Predicate<? super T> predicate) {
        return value -> predicate.test(value) || ForgeHooks.hasNoElements((Ingredient) value);
    }
}
