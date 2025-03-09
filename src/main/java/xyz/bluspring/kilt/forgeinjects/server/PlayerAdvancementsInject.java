package xyz.bluspring.kilt.forgeinjects.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.AdvancementLoadFix;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsInject {
    @Shadow @Final private Set<Advancement> visible;

    @Shadow @Final private Set<Advancement> visibilityChanged;

    @Shadow @Final private Map<Advancement, AdvancementProgress> advancements;

    @Shadow @Final private Set<Advancement> progressChanged;

    @Shadow protected abstract boolean shouldBeVisible(Advancement advancement);

    @Shadow private ServerPlayer player;

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;ensureAllVisible()V"))
    private void kilt$checkFixAdvancementLoading(PlayerAdvancements instance, Operation<Void> original) {
        if (ForgeConfig.SERVER.fixAdvancementLoading.get())
            AdvancementLoadFix.loadVisibility((PlayerAdvancements) (Object) this, this.visible, this.visibilityChanged, this.advancements, this.progressChanged, this::shouldBeVisible);
        else
            original.call(instance);
    }

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
        ForgeHooks.onAdvancement(this.player, advancement);
        ForgeEventFactory.onAdvancementEarnedEvent(this.player, advancement);
    }

    @Inject(method = "revoke", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void kilt$callAdvancementProgressedRevoke(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir, @Local AdvancementProgress advancementProgress) {
        ForgeEventFactory.onAdvancementProgressedEvent(this.player, advancement, advancementProgress, criterionKey, AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE);
    }
}
