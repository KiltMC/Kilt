package xyz.bluspring.kilt.forgeinjects.server;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsInject {
    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void kilt$cancelIfFakePlayer(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        if (this.player instanceof FakePlayer)
            cir.setReturnValue(false);
    }

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void kilt$callAdvancementProgressed(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir, @Local AdvancementProgress advancementProgress) {
        ForgeEventFactory.onAdvancementProgressedEvent(this.player, advancement, advancementProgress, criterionKey, AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT);
    }

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void kilt$callAdvancementEarned(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        ForgeEventFactory.onAdvancementEarnedEvent(this.player, advancement);
    }

    @Inject(method = "revoke", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void kilt$callAdvancementProgressedRevoke(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir, @Local AdvancementProgress advancementProgress) {
        ForgeEventFactory.onAdvancementProgressedEvent(this.player, advancement, advancementProgress, criterionKey, AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE);
    }
}