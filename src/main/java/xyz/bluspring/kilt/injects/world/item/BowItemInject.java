package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.BowItemInjection;

@Mixin(BowItem.class)
public abstract class BowItemInject implements BowItemInjection {
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"), cancellable = true)
    private void kilt$checkUseDurationEvent(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo ci, @Local Player player, @Local(ordinal = 1) ItemStack projectile, @Local(ordinal = 1) LocalIntRef chargeProgress) {
        chargeProgress.set(EventHooks.onArrowLoose(stack, level, player, chargeProgress.get(), !projectile.isEmpty()));

        if (chargeProgress.get() < 0)
            ci.cancel();
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean kilt$callArrowNockEvent(boolean flag, Level level, Player player, InteractionHand usedHand, @Local ItemStack bow, @Cancellable CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        var result = EventHooks.onArrowNock(bow, level, player, usedHand, flag);

        if (result != null)
            cir.setReturnValue(result);

        return flag;
    }
}
