package xyz.bluspring.kilt.injects.world.entity.projectile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlInject extends ThrowableItemProjectile {
    public ThrownEnderpearlInject(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z"))
    private boolean kilt$callPearlLandEvent(ServerPlayer instance, Operation<Boolean> original, @Share("event") LocalRef<EntityTeleportEvent.EnderPearl> eventRef, @Local(argsOnly = true) HitResult hitResult) {
        var result = original.call(instance);

        if (!result) {
            eventRef.set(EventHooks.onEnderPearlLand(instance, this.getX(), this.getY(), this.getZ(), (ThrownEnderpearl) (Object) this, 5f, hitResult));

            return eventRef.get().isCanceled();
        }

        return true;
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private void kilt$teleportEntityToEventTarget(HitResult result, CallbackInfo ci, @Share("event") LocalRef<EntityTeleportEvent.EnderPearl> eventRef, @Local Entity entity) {
        if (eventRef.get() != null)
            entity.teleportTo(eventRef.get().getTargetX(), eventRef.get().getTargetY(), eventRef.get().getTargetZ());
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float kilt$useEventAttackDamage(float damage, @Share("event") LocalRef<EntityTeleportEvent.EnderPearl> eventRef) {
        if (eventRef.get() != null) {
            return eventRef.get().getAttackDamage();
        }

        return damage;
    }

    // TODO: figure out teleporter
}
