package xyz.bluspring.kilt.mixin.workarounds.crafting_remainders;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(FabricItem.class)
public interface FabricItemMixin {

    @Inject(method = "getRecipeRemainder", at = @At("RETURN"), cancellable = true)
    private void kilt$getForgeRecipeRemainder(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (
            KiltHelper.INSTANCE.hasMethodOverride(
                stack.getItem().getClass(), IForgeItem.class,
                "hasCraftingRemainingItem", ItemStack.class
            ) ||
            KiltHelper.INSTANCE.hasMethodOverride(
                stack.getItem().getClass(), IForgeItem.class,
                "getCraftingRemainingItem", ItemStack.class
            )
        ) {
            if (stack.hasCraftingRemainingItem()) {
                cir.setReturnValue(stack.getCraftingRemainingItem());
            }
        }
    }

}
