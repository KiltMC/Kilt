package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public abstract class BundleItemInject {
    @Definition(id = "SECONDARY", field = "Lnet/minecraft/world/inventory/ClickAction;SECONDARY:Lnet/minecraft/world/inventory/ClickAction;")
    @Expression("? != SECONDARY")
    @ModifyExpressionValue(method = "overrideStackedOnOther", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsCountNotOne(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return stack.getCount() != 1 || original;
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIsCountNotOne(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getCount() != 1)
            cir.setReturnValue(false);
    }
}
