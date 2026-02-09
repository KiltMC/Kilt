package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.fluids.FluidType;
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
    @Shadow public abstract ItemStack getBodyArmorItem();
    @Shadow public abstract boolean isBodyArmorItem(ItemStack itemStack);

    @Unique @Nullable private MobSpawnType spawnType;
    @Unique private boolean spawnCancelled = false;

    protected MobInject(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity kilt$callChangeTargetEvent(LivingEntity value) {
        LivingChangeTargetEvent event = CommonHooks.onLivingChangeTarget(this, value, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);

        if (!event.isCanceled()) {
            return event.getNewAboutToBeSetTarget();
        }

        return null;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kilt$handleAnimalArmorTick(CallbackInfo ci) {
        if (this.canUseSlot(EquipmentSlot.BODY)) {
            ItemStack stack = this.getBodyArmorItem();
            if (this.isBodyArmorItem(stack)) {
                stack.onAnimalArmorTick(this.level(), (Mob) (Object) this);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void kilt$saveSpawnType(CompoundTag compound, CallbackInfo ci) {
        if (this.spawnType != null) {
            compound.putString("neoforge:spawn_type", this.spawnType.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void kilt$loadSpawnType(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("neoforge:spawn_type")) {
            try {
                this.spawnType = MobSpawnType.valueOf(compound.getString("neoforge:spawn_type"));
            } catch (Exception ignored) {
                compound.remove("neoforge:spawn_type");
            }
        }
    }

    @Definition(id = "getBoolean", method = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
    @Definition(id = "level", method = "Lnet/minecraft/world/entity/Mob;level()Lnet/minecraft/world/level/Level;")
    @Definition(id = "getGameRules", method = "Lnet/minecraft/world/level/Level;getGameRules()Lnet/minecraft/world/level/GameRules;")
    @Definition(id = "RULE_MOBGRIEFING", field = "Lnet/minecraft/world/level/GameRules;RULE_MOBGRIEFING:Lnet/minecraft/world/level/GameRules$Key;")
    @Expression("this.level().getGameRules().getBoolean(RULE_MOBGRIEFING)")
    @ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanEntityGrief(boolean original) {
        return original || EventHooks.canEntityGrief(this.level(), this);
    }

    @ModifyReceiver(method = "getApproximateAttackDamageWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;compute(DLnet/minecraft/world/entity/EquipmentSlot;)D"))
    private ItemAttributeModifiers kilt$tryComputeAttributes(ItemAttributeModifiers instance, double d, EquipmentSlot equipmentSlot, @Local(argsOnly = true) ItemStack stack) {
        return stack.kilt$getAttributeModifiers(instance);
    }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void kilt$checkMobDespawnEvent(CallbackInfo ci) {
        if (EventHooks.checkMobDespawn((Mob) (Object) this))
            ci.cancel();
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void kilt$storeCurrentSpawnType(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        this.spawnType = spawnType;
    }

    @Override
    public void jumpInFluid(FluidType type) {
        // Kilt TODO: make this correct
        this.jumpInLiquid(FluidTags.WATER);
    }

    @Override
    public final @Nullable MobSpawnType getSpawnType() {
        return this.spawnType;
    }

    @Override
    public final boolean isSpawnCancelled() {
        return this.spawnCancelled;
    }

    @Override
    public final void setSpawnCancelled(boolean spawnCancelled) {
        if (this.isAddedToLevel()) {
            throw new UnsupportedOperationException("Late invocations of Mob#setSpawnCancelled are not permitted.");
        }

        this.spawnCancelled = spawnCancelled;
    }
}
