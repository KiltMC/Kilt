package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Recipe.class)
public interface RecipeInject {
    @Inject(at = @At("HEAD"), method = "method_31583", cancellable = true)
    private static void kilt$useForgeNoElementsCheck(Ingredient ingredient, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ForgeHooks.hasNoElements(ingredient));
    }
}
