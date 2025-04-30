package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuInject {
    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0), require = 0)
    private int kilt$disableCountCheck(ItemStack instance) {
        return 1;
    }

    @Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot")
    public abstract static class IngredientsSlotInject {
        @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
        private boolean kilt$checkIsValidInput(boolean original, @Local(argsOnly = true) ItemStack stack) {
            return original || BrewingRecipeRegistry.isValidIngredient(stack);
        }
    }

    @Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
    public abstract static class PotionSlotInject {
        @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/BrewedPotionTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/alchemy/Potion;)V"))
        private void kilt$callForgeBrewedPotionEvent(Player player, ItemStack stack, CallbackInfo ci) {
            ForgeEventFactory.onPlayerBrewedPotion(player, stack);
        }

        @ModifyReturnValue(method = "mayPlaceItem", at = @At("RETURN"))
        private static boolean kilt$checkIsValidInput(boolean original, @Local(argsOnly = true) ItemStack stack) {
            return original || BrewingRecipeRegistry.isValidInput(stack);
        }
    }
}
