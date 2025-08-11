// TRACKED HASH: 509c9dc95ab8642482abd57150c1cc2f0c792d50
package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.bus.api.Event;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.MobInjection;

@Mixin(Mob.class)
public abstract class MobInject extends LivingEntity implements MobInjection {
    @Shadow public abstract PathNavigation getNavigation();

    @Nullable @Unique private MobSpawnType spawnType;
    @Unique private boolean spawnCancelled = false;

    protected MobInject(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity kilt$tryCallChangeTargetEvent(LivingEntity value, @Cancellable CallbackInfo ci) {
        var event = CommonHooks.onLivingChangeTarget(this, value, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);

        if (!event.isCanceled()) {
            return event.getNewTarget();
        } else {
            ci.cancel();
        }

        return value;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void kilt$storeCurrentSpawnType(CompoundTag compound, CallbackInfo ci) {
        if (this.spawnType != null) {
            compound.putString("forge:spawn_type", this.spawnType.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void kilt$loadCurrentSpawnType(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("forge:spawn_type")) {
            try {
                this.spawnType = MobSpawnType.valueOf(compound.getString("forge:spawn_type"));
            } catch (Exception ignored) {
                compound.remove("forge:spawn_type");
            }
        }
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanInvokeMobGriefing(boolean original) {
        return original || EventHooks.getMobGriefingEvent(this.level(), this);
    }

    @ModifyExpressionValue(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/entity/player/Player;"))
    private Player kilt$checkCanEntityDespawn(Player original) {
        var result = EventHooks.canEntityDespawn((Mob) (Object) this, (ServerLevelAccessor) this.level());

        if (result == Event.Result.DENY) {
            this.noActionTime = 0;
            return null;
        } else if (result == Event.Result.ALLOW) {
            this.discard();
            return null;
        }

        return original;
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void kilt$setSpawnType(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        this.spawnType = reason;
    }

    @Override
    public void jumpInFluid(FluidType type) {
        if (this.getNavigation().canFloat()) {
            super.jumpInFluid(type);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    @Override
    public MobSpawnType getSpawnType() {
        return this.spawnType;
    }

    @Override
    public boolean isSpawnCancelled() {
        return this.spawnCancelled;
    }

    @Override
    public void setSpawnCancelled(boolean cancel) {
        if (this.isAddedToWorld()) {
            throw new UnsupportedOperationException("Late invocations of Mob#setSpawnCancelled are not permitted.");
        }

        this.spawnCancelled = cancel;
    }
}