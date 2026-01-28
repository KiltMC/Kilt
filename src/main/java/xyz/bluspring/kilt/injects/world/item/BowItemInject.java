package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
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
    @Shadow public abstract int getUseDuration(ItemStack stack);

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.BY, by = 2), cancellable = true)
    private void kilt$checkUseDurationEvent(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo ci, @Local Player player, @Local(ordinal = 1) ItemStack projectile, @Local boolean flag) {
        int chargeProgress = this.getUseDuration(stack) - timeCharged;
        chargeProgress = EventHooks.onArrowLoose(stack, level, player, chargeProgress, !projectile.isEmpty() || flag);

        if (chargeProgress < 0)
            ci.cancel();
    }

    // Kilt: Handled by Porting Lib
    /*@ModifyVariable(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", shift = At.Shift.AFTER), ordinal = 1)
    private boolean kilt$modifyUseIsInfiniteCheck(boolean value, @Local Player player, @Local(ordinal = 1) ItemStack projectile, @Local(argsOnly = true) ItemStack stack) {
        return value || player.getAbilities().instabuild || (projectile.getItem() instanceof ArrowItem arrowItem && ((ArrowItemInjection) arrowItem).isInfinite(projectile, stack, player));
    }*/

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean kilt$callArrowNockEvent(boolean flag, Level level, Player player, InteractionHand usedHand, @Local ItemStack bow, @Cancellable CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        var result = EventHooks.onArrowNock(bow, level, player, usedHand, flag);

        if (result != null)
            cir.setReturnValue(result);

        return flag;
    }
}
