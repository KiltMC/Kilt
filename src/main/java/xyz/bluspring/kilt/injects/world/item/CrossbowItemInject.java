package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemInject {
    @Inject(method = "performShooting", at = @At("HEAD"), cancellable = true)
    private static void kilt$checkForgeArrowLooseEvent(Level level, LivingEntity shooter, InteractionHand usedHand, ItemStack crossbowStack, float velocity, float inaccuracy, CallbackInfo ci) {
        if (shooter instanceof Player player && EventHooks.onArrowLoose(crossbowStack, level, player, 1, true) < 0)
            ci.cancel();
    }
}
