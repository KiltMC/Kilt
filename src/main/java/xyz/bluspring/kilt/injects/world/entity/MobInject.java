package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.MobInjection;
import xyz.bluspring.kilt.workarounds.KiltFluidTags;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(Mob.class)
public abstract class MobInject extends LivingEntity implements MobInjection {
    @Shadow private @Nullable LivingEntity target;
    @Shadow public abstract ItemStack getBodyArmorItem();

    @Unique @Nullable private EntitySpawnReason spawnType;
    @Unique private boolean spawnCancelled = false;
    @Unique protected boolean preventConversionSpawns = false;

    protected MobInject(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(method = "setTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;asValidTarget(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/LivingEntity;"))
    private LivingEntity kilt$tryHandleTargetChangeEvent(LivingEntity original) {
        var changeTargetEvent = CommonHooks.onLivingChangeTarget(this, original, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);

        if (!changeTargetEvent.isCanceled()) {
            return changeTargetEvent.getNewAboutToBeSetTarget();
        }

        return this.target;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kilt$handleArmorTick(CallbackInfo ci) {
        if (this.canUseSlot(EquipmentSlot.BODY)) {
            ItemStack stack = this.getBodyArmorItem();
            if (stack.has(DataComponents.EQUIPPABLE))
                stack.onAnimalArmorTick(this.level(), (Mob) (Object) this);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void kilt$storeSpawnType(ValueOutput output, CallbackInfo ci) {
        if (this.spawnType != null)
            output.putString("neoforge:spawn_type", this.spawnType.name());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void kilt$readSpawnType(ValueInput input, CallbackInfo ci) {
        input.getString("neoforge:spawn_type")
            .ifPresent(spawnType -> {
                try {
                    this.spawnType = EntitySpawnReason.valueOf(spawnType);
                } catch (Exception _) {}
            });
    }

    @Definition(id = "Boolean", type = Boolean.class)
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkMobGriefing(Boolean original, @Local(name = "serverLevel") ServerLevel serverLevel) {
        return original && EventHooks.canEntityGrief(serverLevel, this);
    }

    @ModifyReceiver(method = "getApproximateAttributeWith", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;compute(Lnet/minecraft/core/Holder;DLnet/minecraft/world/entity/EquipmentSlot;)D"))
    private ItemAttributeModifiers kilt$tryUseStackModifiers(ItemAttributeModifiers instance, Holder<Attribute> attribute, double baseValue, EquipmentSlot slot, @Local(argsOnly = true, name = "itemStack") ItemStack stack) {
        return stack.kilt$getAttributeModifiers(instance);
    }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void kilt$checkMobDespawn(CallbackInfo ci) {
        if (EventHooks.checkMobDespawn((Mob) (Object) this))
            ci.cancel();
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void kilt$storeSpawnType(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        this.spawnType = spawnReason;
    }

    @Unique private Runnable kilt$onSuper;

    @Override
    public void jumpInFluid(FluidType type) {
        this.jumpInLiquidInternal(() -> super.jumpInFluid(type));
    }

    @WrapOperation(method = "jumpInLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpInLiquid(Lnet/minecraft/tags/TagKey;)V"))
    private void kilt$tryHandleSuperJumpInFluid(Mob instance, TagKey<Fluid> type, Operation<Void> original) {
        if (this.kilt$onSuper != null)
            this.kilt$onSuper.run();
        else
            original.call(instance, type);
    }

    @Unique
    private void jumpInLiquidInternal(Runnable onSuper) {
        this.kilt$onSuper = onSuper;

        this.jumpInLiquid(KiltFluidTags.EMPTY);

        this.kilt$onSuper = null;
    }

    @Override
    public @Nullable EntitySpawnReason getSpawnType() {
        return this.spawnType;
    }

    @Override
    public boolean isSpawnCancelled() {
        return this.spawnCancelled;
    }

    @Override
    public void setSpawnCancelled(boolean spawnCancelled) {
        if (this.isAddedToLevel()) {
            throw new UnsupportedOperationException("Late invocations of Mob#setSpawnCancelled are not permitted.");
        }

        this.spawnCancelled = spawnCancelled;
    }
}
