// TRACKED HASH: 4a532f665aabef3c36a9818c82e846213791c292
package xyz.bluspring.kilt.injects.client.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.player.LocalPlayerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerInject extends AbstractClientPlayer implements LocalPlayerInjection {
    public LocalPlayerInject(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Shadow public abstract boolean isUsingItem();
    @Shadow public abstract InteractionHand getUsedItemHand();
    @Shadow public abstract void stopUsingItem();

    @Shadow public Input input;

    @Inject(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;"))
    private void kilt$fixMC231097(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isUsingItem() && this.getUsedItemHand() == InteractionHand.MAIN_HAND && (fullStack || this.getUseItem().getCount() == 1))
            this.stopUsingItem();
    }

    @WrapOperation(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void kilt$handleForgePlaySoundEvent(Level instance, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        var holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        var event = EventHooks.onPlaySoundAtEntity(this, holder, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null)
            return;

        original.call(instance, x, y, z, event.getSound().value(), event.getSource(), event.getNewVolume(), event.getNewPitch(), distanceDelay);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/Input;)V"))
    private void kilt$handleForgeMovementUpdate(CallbackInfo ci) {
        ClientHooks.onMovementInputUpdate(this, this.input);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 0))
    private boolean kilt$checkCanSwim(boolean original) {
        return original || this.canStartSwimming();
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 1))
    private boolean kilt$checkCanSwim2(boolean original) {
        return original || this.canStartSwimming();
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 0))
    private boolean kilt$checkCanSwimInFluidType(boolean original) {
        return original || this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType));
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 2))
    private boolean kilt$checkCanSwimAndInFluidType(boolean original) {
        return original && !(this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType)) && !this.canStartSwimming());
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 2))
    private boolean kilt$checkCanSwimInFluidType2(boolean original) {
        return original || this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType));
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkCanElytraFly(ItemStack instance, Item item, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), IItemExtension.class, "canElytraFly", ItemStack.class, LivingEntity.class)) {
            return instance.canElytraFly(this);
        }

        return original.call(instance, item);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 3))
    private boolean kilt$checkCanSwimInFluidType3(boolean original, @Share("fluidType") LocalRef<FluidType> fluidType) {
        fluidType.set(this.getMaxHeightFluidType());
        return original || (!fluidType.get().isAir() && this.canSwimInFluidType(fluidType.get()));
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;goDownInWater()V"))
    private void kilt$trySinkInFluid(LocalPlayer instance, Operation<Void> original, @Share("fluidType") LocalRef<FluidType> fluidType) {
        if (fluidType.get() == null /*|| fluidType.get().kilt$isWrapped*/ || fluidType.get().isVanilla()) {
            original.call(instance);
        } else {
            this.sinkInFluid(fluidType.get());
        }
    }

    @Inject(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;rideTick()V", shift = At.Shift.AFTER))
    private void kilt$disableShiftKeyInputFromExit(CallbackInfo ci) {
        // don't question the devs i guess? idk why this is needed
        if (this.wantsToStopRiding() && this.isPassenger())
            this.input.shiftKeyDown = false;
    }
}
