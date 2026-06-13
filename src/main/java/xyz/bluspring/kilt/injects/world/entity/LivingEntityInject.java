// TRACKED HASH: 0103ffc8bca3b91dd898021eb13bdca66921d3eb
package xyz.bluspring.kilt.injects.world.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingDrownEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.LivingEntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(LivingEntity.class)
public abstract class LivingEntityInject extends Entity implements ILivingEntityExtension, LivingEntityInjection {
    public LivingEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean isAlive();
    @Shadow @Nullable protected Player lastHurtByPlayer;
    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);
    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;
    @Shadow protected abstract void onEffectRemoved(MobEffectInstance effectInstance);
    @Shadow private boolean effectsDirty;
    @Shadow protected ItemStack useItem;
    @Shadow protected int useItemRemaining;
    @Shadow public abstract int getUseItemRemainingTicks();
    @Shadow protected float lastHurt;
    @Shadow private Optional<BlockPos> lastClimbablePos;
    @Shadow public abstract ItemStack getMainHandItem();
    @Shadow public abstract boolean isUsingItem();
    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);
    @Shadow protected int fallFlyTicks;
    @Shadow protected abstract int increaseAirSupply(int currentAir);
    @Shadow protected abstract int decreaseAirSupply(int currentAir);

    @Nullable
    protected Stack<DamageContainer> damageContainers = new Stack<>();

    @Override
    public Stack<DamageContainer> kilt$getDamageContainers() {
        return this.damageContainers;
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder kilt$addNeoAttributes(AttributeSupplier.Builder original) {
        return original
            .add(NeoForgeMod.SWIM_SPEED)
            .add(NeoForgeMod.NAMETAG_DISTANCE);
    }

    @WrapOperation(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions> int kilt$checkIfShouldSpawnParticles(ServerLevel instance, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, Operation<Integer> original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) BlockPos pos, @Local int i) {
        if (!state.addLandingEffects(instance, pos, state, (LivingEntity) (Object) this, i)) {
            return original.call(instance, type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
        }

        return 0;
    }

    // Kilt: we're reimplementing onLivingBreathe from scratch here for the sake of mod compatibility
    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private void kilt$tryHandleLivingBreathe(CallbackInfo ci, @Share("breatheEvent") LocalRef<LivingBreatheEvent> breatheEventRef) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int airSupply = this.getAirSupply();
        int consumeAirAmount = airSupply - this.decreaseAirSupply(airSupply);
        int refillAirAmount = this.increaseAirSupply(airSupply) - airSupply;

        // Check things that vanilla considers to be air - these will cause the air supply to be increased.
        boolean isAir = entity.getEyeInFluidType().isAir() || entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ())).is(Blocks.BUBBLE_COLUMN);
        boolean canBreathe = isAir;
        // The following effects cause the entity to not drown, but do not cause the air supply to be increased.
        if (!isAir && (MobEffectUtil.hasWaterBreathing(entity) || !entity.canDrownInFluidType(entity.getEyeInFluidType()) || (entity instanceof Player player && player.getAbilities().invulnerable))) {
            canBreathe = true;
            refillAirAmount = 0;
        }

        LivingBreatheEvent breatheEvent = new LivingBreatheEvent(entity, canBreathe, consumeAirAmount, refillAirAmount);
        NeoForge.EVENT_BUS.post(breatheEvent);
        breatheEventRef.set(breatheEvent);

        if (breatheEvent.kilt$canBreatheModified) {
            if (breatheEvent.canBreathe()) {
                entity.setAirSupply(Math.min(entity.getAirSupply() + breatheEvent.getRefillAirAmount(), entity.getMaxAirSupply()));
            } else {
                entity.setAirSupply(entity.getAirSupply() - breatheEvent.getConsumeAirAmount());
            }
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean kilt$checkIsDrownableFluid(LivingEntity instance, TagKey tagKey, Operation<Boolean> original) {
        return original.call(instance, tagKey) || instance.canDrownInFluidType(instance.getEyeInFluidType());
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAirSupply(I)V", ordinal = 0))
    private void kilt$tryHandleAirSupplyDecreaseChanges(LivingEntity instance, int i, Operation<Void> original, @Share("breatheEvent") LocalRef<LivingBreatheEvent> breatheEventRef) {
        var breatheEvent = breatheEventRef.get();

        if (breatheEvent != null && breatheEvent.kilt$isConsumeModified) {
            original.call(instance, instance.getAirSupply() - breatheEvent.getConsumeAirAmount());
        } else {
            original.call(instance, i);
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAirSupply(I)V", ordinal = 2))
    private void kilt$tryHandleAirSupplyIncreaseChanges(LivingEntity instance, int i, Operation<Void> original, @Share("breatheEvent") LocalRef<LivingBreatheEvent> breatheEventRef) {
        var breatheEvent = breatheEventRef.get();

        if (breatheEvent != null && breatheEvent.kilt$isRefillModified) {
            original.call(instance, Math.min(instance.getAirSupply() + breatheEvent.getRefillAirAmount(), instance.getMaxAirSupply()));
        } else {
            original.call(instance, i);
        }
    }

    @Definition(id = "getAirSupply", method = "Lnet/minecraft/world/entity/LivingEntity;getAirSupply()I")
    @Expression("this.getAirSupply() == -20")
    @Inject(method = "baseTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$initDrownEvent(CallbackInfo ci, @Share("drownEvent") LocalRef<LivingDrownEvent> eventRef) {
        eventRef.set(new LivingDrownEvent((LivingEntity) (Object) this));
    }

    @Definition(id = "getAirSupply", method = "Lnet/minecraft/world/entity/LivingEntity;getAirSupply()I")
    @Expression("this.getAirSupply() == -20")
    @ModifyExpressionValue(method = "baseTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callDrownEvent(boolean original, @Share("drownEvent") LocalRef<LivingDrownEvent> eventRef) {
        var drownEvent = eventRef.get();
        if (drownEvent == null) {
            drownEvent = new LivingDrownEvent((LivingEntity) (Object) this);
            eventRef.set(drownEvent);
        }

        return !NeoForge.EVENT_BUS.post(drownEvent).isCanceled() && drownEvent.isDrowning();
    }

    @Expression("? < @(8)")
    @ModifyExpressionValue(method = "baseTick", at = @At("MIXINEXTRAS:EXPRESSION"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;decreaseAirSupply(I)I"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;drown()Lnet/minecraft/world/damagesource/DamageSource;")))
    private int kilt$tryUseEventBubbleCount(int original, @Share("drownEvent") LocalRef<LivingDrownEvent> eventRef) {
        var drownEvent = eventRef.get();
        if (drownEvent != null && drownEvent.kilt$bubbleCountModified) {
            return drownEvent.getBubbleCount();
        }

        return original;
    }

    @Definition(id = "hurt", method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    @Definition(id = "drown", method = "Lnet/minecraft/world/damagesource/DamageSources;drown()Lnet/minecraft/world/damagesource/DamageSource;")
    @Expression("this.hurt(?.drown(), ?)")
    @WrapOperation(method = "baseTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$wrapWithDamageCheck(LivingEntity instance, DamageSource source, float amount, Operation<Boolean> original, @Share("drownEvent") LocalRef<LivingDrownEvent> eventRef) {
        var drownEvent = eventRef.get();
        if (drownEvent != null && drownEvent.getDamageAmount() <= 0) {
            return false;
        }

        return original.call(instance, source, amount);
    }

    @WrapOperation(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;isInPowderSnow:Z"))
    private boolean kilt$checkIfCanExtinguish(LivingEntity instance, Operation<Boolean> original) {
        return original.call(instance) || instance.isInFluidType((fluidType, height) -> instance.canFluidExtinguish(fluidType));
    }

    @ModifyExpressionValue(method = "tickEffects", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", ordinal = 0))
    private boolean kilt$checkIfEffectExpired(boolean original, @Local MobEffectInstance effect) {
        if (!original) {
            return NeoForge.EVENT_BUS.post(new MobEffectEvent.Expired((LivingEntity) (Object) this, effect)).isCanceled();
        }
        return true;
    }

    @WrapOperation(method = "updateSynchronizedMobEffectParticles", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private <T extends MobEffectInstance> Stream<T> kilt$callAndUseEffectParticleModifyEvent(Stream<T> instance, Predicate<? super T> predicate, Operation<Stream<T>> original, @Share("events") LocalRef<Map<MobEffectInstance, EffectParticleModificationEvent>> eventsRef) {
        eventsRef.set(new HashMap<>());
        LivingEntity self = (LivingEntity) (Object) this;

        return original.call(instance.peek(effect -> eventsRef.get().put(effect, NeoForge.EVENT_BUS.post(new EffectParticleModificationEvent(self, effect)))),
            (Predicate<T>) effect -> {
                var event = eventsRef.get().get(effect);

                if (event.kilt$wasVisibilityModified()) {
                    return event.isVisible();
                }

                return predicate.test(effect);
            });
    }

    @WrapOperation(method = "updateSynchronizedMobEffectParticles", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private <T extends MobEffectInstance, R extends ParticleOptions> Stream<R> kilt$tryGetEventParticleOptions(Stream<T> instance, Function<? super T, ? extends R> function, Operation<Stream<R>> original, @Share("events") LocalRef<Map<MobEffectInstance, EffectParticleModificationEvent>> eventsRef) {
        return original.call(instance, (Function<? super T, ? extends R>) effect -> {
            var event = eventsRef.get().get(effect);

            if (event.getOriginalParticleOptions() != event.getParticleOptions()) {
                return (R) event.getParticleOptions();
            }

            return function.apply(effect);
        });
    }

    @ModifyReturnValue(method = "getVisibilityPercent", at = @At("RETURN"))
    private double kilt$modifyVisibilityMultiplier(double original, @Local(argsOnly = true) Entity lookingEntity) {
        return CommonHooks.getEntityVisibilityMultiplier((LivingEntity) (Object) this, lookingEntity, original);
    }

    @WrapWithCondition(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    private boolean kilt$callRemoveEffectEvent(LivingEntity instance, MobEffectInstance effectInstance, @Share("shouldRemove") LocalBooleanRef shouldCancel) {
        if (EventHooks.onEffectRemoved(instance, effectInstance, null)) {
            shouldCancel.set(true);
            return false;
        }

        return true;
    }

    @WrapWithCondition(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private boolean kilt$checkIfCancelledAlready(Iterator<?> instance, @Share("shouldRemove") LocalBooleanRef shouldCancel) {
        return !shouldCancel.get();
    }

    @WrapOperation(method = {"addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", "forceAddEffect"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canBeAffected(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean kilt$tryCheckCanEffectBeApplied(LivingEntity instance, MobEffectInstance effectInstance, Operation<Boolean> original, @Local(argsOnly = true) Entity entity) {
        return original.call(instance, effectInstance) || CommonHooks.canMobEffectBeApplied(instance, effectInstance, entity);
    }

    @WrapOperation(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V kilt$callAddEffectEvent(Map<K, V> instance, K o, Operation<V> original, @Local(argsOnly = true) MobEffectInstance newEffect, @Local(argsOnly = true) Entity entity) {
        var oldEffect = (MobEffectInstance) original.call(instance, o);

        NeoForge.EVENT_BUS.post(new MobEffectEvent.Added((LivingEntity) (Object) this, oldEffect, newEffect, entity));
        return (V) oldEffect;
    }

    @Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
    private void kilt$checkRemoveEffect(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (EventHooks.onEffectRemoved((LivingEntity) (Object) this, effect, null))
            cir.setReturnValue(false);
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float kilt$callHealEvent(float value) {
        return EventHooks.onLivingHeal((LivingEntity) (Object) this, value);
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIfHealValueIsNegative(float healAmount, CallbackInfo ci) {
        if (healAmount <= 0)
            ci.cancel();
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void kilt$pushNewDamageContainer(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damageContainers.push(new DamageContainer(source, amount));

        if (CommonHooks.onEntityIncomingDamage((LivingEntity) (Object) this, damageContainers.peek()))
            cir.setReturnValue(false);
    }

    @ModifyVariable(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I", shift = At.Shift.AFTER), argsOnly = true)
    private float kilt$modifyDamage(float value) {
        DamageContainer container = this.damageContainers.peek();
        if (value != container.getOriginalDamage())
            return value;

        return container.getNewDamage();
    }

    @ModifyExpressionValue(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean kilt$checkIsDamageBlocked(boolean original, @Share("shieldEvent") LocalRef<LivingShieldBlockEvent> shieldEvent) {
        shieldEvent.set(CommonHooks.onDamageBlock((LivingEntity) (Object) this, damageContainers.peek(), original));

        return shieldEvent.get().getBlocked();
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void kilt$setBlockedDamageToContainer(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("shieldEvent") LocalRef<LivingShieldBlockEvent> shieldEvent) {
        damageContainers.peek().setBlockedDamage(shieldEvent.get());
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void kilt$checkShouldHurtCurrentShield(LivingEntity instance, float damageAmount, Operation<Void> original, @Share("shieldEvent") LocalRef<LivingShieldBlockEvent> shieldEvent) {
        damageContainers.peek().setBlockedDamage(shieldEvent.get());

        if (damageAmount != shieldEvent.get().getOriginalBlockedDamage()) {
            original.call(instance, damageAmount); // Ensure modded damage goes through instead of ours.
        } else if (shieldEvent.get().shieldDamage() > 0) {
            original.call(instance, shieldEvent.get().shieldDamage());
        }
    }

    @Definition(id = "f", local = @Local(type = float.class, ordinal = 2))
    @Definition(id = "amount", local = @Local(type = float.class, ordinal = 0, argsOnly = true))
    @Expression("f = @(amount)")
    @ModifyExpressionValue(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float kilt$modifyTotalBlockedDamage(float original, @Share("shieldEvent") LocalRef<LivingShieldBlockEvent> shieldEvent) {
        if (original != shieldEvent.get().getOriginalBlockedDamage()) {
            return original;
        }

        return shieldEvent.get().getBlockedDamage();
    }

    @Definition(id = "amount", local = @Local(type = float.class, ordinal = 0, argsOnly = true))
    @Expression("amount = @(0.0)")
    @ModifyExpressionValue(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float kilt$modifyTotalDamage(float original, @Share("shieldEvent") LocalRef<LivingShieldBlockEvent> shieldEvent) {
        if (original != 0.0) {
            return original;
        }

        return shieldEvent.get().getDamageContainer().getNewDamage();
    }

    @Definition(id = "bl", local = @Local(type = boolean.class, ordinal = 0))
    @Expression("bl = @(true)")
    @ModifyExpressionValue(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsDamageAmountFullyBlocked(boolean original, @Local(argsOnly = true) float damage) {
        return original && damage <= 0;
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;setSpeed(F)V"))
    private void kilt$updateContainerWithVanillaChanges(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damageContainers.peek().setNewDamage(amount); //update container with vanilla changes
    }

    @Inject(method = "hurt", at = @At(value = "RETURN", ordinal = 4))
    private void kilt$popContainerFromStack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damageContainers.pop();
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", ordinal = 0))
    private void kilt$setContainerReductionByInvulnerability(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damageContainers.peek().setReduction(DamageContainer.Reduction.INVULNERABILITY, this.lastHurt);
    }

    @Definition(id = "invulnerableTime", field = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I")
    @Expression("this.invulnerableTime = @(20)")
    @ModifyExpressionValue(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$modifyPostAttackInvulnerabilityTicks(int original) {
        DamageContainer container = damageContainers.peek();

        if (original != 20) {
            return original;
        }

        return container.getPostAttackInvulnerabilityTicks();
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void kilt$storeInitialExpectedAmount(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("expectedAmount") LocalFloatRef expectedAmount) {
        expectedAmount.set(amount);
    }

    @Definition(id = "amount", local = @Local(type = float.class, ordinal = 0, argsOnly = true))
    @Expression("amount = ?")
    @Inject(method = "hurt", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$updateExpectedAmount(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("expectedAmount") LocalFloatRef expectedAmount) {
        expectedAmount.set(amount);
    }

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"), argsOnly = true)
    private float kilt$updateLocalAmountWithContainer(float original, @Share("expectedAmount") LocalFloatRef expectedAmount) {
        DamageContainer container = damageContainers.peek();

		if (original != expectedAmount.get()) {
			return original;
		}

        return container.getNewDamage();
    }

    // TODO: implement TamableAnimal instanceof check

    @Inject(method = "hurt", at = @At("TAIL"))
    private void kilt$popDamageContainer(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.damageContainers.pop();
    }

    @WrapOperation(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private boolean kilt$checkTotemEvent(ItemStack instance, Item item, Operation<Boolean> original, @Local(argsOnly = true) DamageSource source, @Local InteractionHand hand) {
        return original.call(instance, item) && CommonHooks.onLivingUseTotem((LivingEntity) (Object) this, source, instance, hand);
    }

    @WrapOperation(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean kilt$removeTotemEffects(LivingEntity instance, Operation<Boolean> original) {
        var effects = this.activeEffects.values();
        var shouldUseCures = false;

        // Kilt: Mod compatibility :D
        for (MobEffectInstance effect : effects) {
            if (!effect.neoforge$getCures().contains(EffectCures.PROTECTED_BY_TOTEM)) {
                shouldUseCures = true;
                break;
            }
        }

        if (shouldUseCures)
            return instance.removeEffectsCuredBy(EffectCures.PROTECTED_BY_TOTEM);
        else
            return original.call(instance);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void kilt$checkLivingDeath(DamageSource damageSource, CallbackInfo ci) {
        if (CommonHooks.onLivingDeath((LivingEntity) (Object) this, damageSource))
            ci.cancel();
    }

    @WrapOperation(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanMobGrief(GameRules instance, GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity entity) {
        return original.call(instance, key) || EventHooks.canEntityGrief(this.level(), entity);
    }

    @WrapOperation(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkIsEmpty(BlockState instance, Operation<Boolean> original, @Local BlockPos pos) {
        return original.call(instance) || this.level().isEmptyBlock(pos);
    }

    // Looting Level and Capture Drops events handled by Porting Lib

    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private int kilt$modifyExperienceReward(int original) {
        return EventHooks.getExperienceDrop((LivingEntity) (Object) this, this.lastHurtByPlayer, original);
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void kilt$modifyKnockback(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalDoubleRef strength, @Local(argsOnly = true, ordinal = 1) LocalDoubleRef x, @Local(argsOnly = true, ordinal = 2) LocalDoubleRef z) {
        var event = CommonHooks.onLivingKnockBack((LivingEntity) (Object) this, (float) strength.get(), x.get(), z.get());

        if (event.isCanceled())
            ci.cancel();

        strength.set(event.getStrength());
        x.set(event.getRatioX());
        z.set(event.getRatioZ());
    }

    @Inject(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0), cancellable = true)
    private void kilt$tryUseNeoLadderPos(CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local BlockPos pos) {
        var ladderPos = CommonHooks.isLivingOnLadder(state, this.level(), pos, (LivingEntity) (Object) this);

        if (ladderPos.isPresent()) {
            this.lastClimbablePos = ladderPos;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIfCancelledFallDamage(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true, ordinal = 0) LocalFloatRef fallDistance, @Local(argsOnly = true, ordinal = 1) LocalFloatRef multiplier) {
        var values = CommonHooks.onLivingFall((LivingEntity) (Object) this, fallDistance.get(), multiplier.get());

        if (values == null) {
            cir.setReturnValue(false);
            return;
        }

        fallDistance.set(values[0]);
        multiplier.set(values[1]);
    }

    // TODO: handle custom Forge sound type

    @Definition(id = "slots", local = @Local(type = EquipmentSlot[].class, argsOnly = true))
    @Expression("slots")
    @Inject(method = "doHurtEquipment", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$useNeoArmorHurt(DamageSource damageSource, float damageAmount, EquipmentSlot[] slots, CallbackInfo ci, @Local int damage) {
        CommonHooks.onArmorHurt(damageSource, slots, damage, (LivingEntity) (Object) this);
    }

    @WrapWithCondition(method = "doHurtEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private boolean kilt$cancelHurtAndBreak(ItemStack instance, int amount, LivingEntity entity, EquipmentSlot slot) {
        // TODO Kilt: i hate this. can we please try to make this more mod compatible.
        return false;
    }

    @Definition(id = "ServerPlayer", type = ServerPlayer.class)
    @Expression("this instanceof ServerPlayer")
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$reduceEffectDamageInContainer(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir, @Local(ordinal = 3) float reduced) {
        this.damageContainers.peek().setReduction(DamageContainer.Reduction.MOB_EFFECTS, reduced);
    }

    @ModifyExpressionValue(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float kilt$reduceEnchantDamageInContainer(float original) {
        this.damageContainers.peek().setReduction(DamageContainer.Reduction.ENCHANTMENTS, this.damageContainers.peek().getNewDamage() - original);
        return original;
    }

    // Kilt: I want you all to know that I really, really, *really* don't like the damage containers system.
    //       It's so terribly convoluted, and it doesn't even make the code particularly extendable.
    //       And it honestly makes the mod compatibility so much more annoying, because dear god I'm so
    //       bloody worried about mixins that are actually using this that we're basically forced to overwrite.

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void kilt$storeOriginalDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Share("originalDamage") LocalFloatRef originalDamage, @Local(argsOnly = true) LocalFloatRef damageRef) {
        originalDamage.set(damageAmount);
        damageRef.set(this.damageContainers.peek().getNewDamage()); // Kilt: just directly use ours, i guess.
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float kilt$tryReduceWithArmorAbsorb(LivingEntity instance, DamageSource damageSource, float damageAmount, Operation<Float> original) {
        DamageContainer container = this.damageContainers.peek();

        var reduced = original.call(instance, damageSource, container.getNewDamage());
        container.setReduction(DamageContainer.Reduction.ARMOR, container.getNewDamage() - reduced);

        return reduced;
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float kilt$tryReduceWithMagicAbsorb(LivingEntity instance, DamageSource damageSource, float damageAmount, Operation<Float> original) {
        return original.call(instance, damageSource, this.damageContainers.peek().getNewDamage());
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private void kilt$callLivingPreDamageEvent(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Share("damage") LocalFloatRef damageRef) {
        damageRef.set(CommonHooks.onLivingDamagePre((LivingEntity) (Object) this, this.damageContainers.peek()));
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float kilt$doAbsorptionModification(float a, float b) {
        return this.damageContainers.peek().getNewDamage();
    }

    @Definition(id = "damageAmount", local = @Local(type = float.class, ordinal = 0, argsOnly = true))
    @Definition(id = "f", local = @Local(type = float.class, ordinal = 1))
    @Expression("f - damageAmount")
    @ModifyExpressionValue(method = "actuallyHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float kilt$useAbsorbedDamage(float original, @Share("damage") LocalFloatRef damageRef) {
        return Math.min(damageRef.get(), this.damageContainers.peek().getReduction(DamageContainer.Reduction.ABSORPTION));
    }

    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V"))
    private float kilt$clampAbsorptionAmount(float absorptionAmount) {
        return Math.max(absorptionAmount, 0);
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER))
    private void kilt$callDamageTaken(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        this.onDamageTaken(this.damageContainers.peek());
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void kilt$callLivingPostDamageEvent(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        CommonHooks.onLivingDamagePost((LivingEntity) (Object) this, this.damageContainers.peek());
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void kilt$checkStackSwing(InteractionHand hand, CallbackInfo ci) {
        var stack = this.getItemInHand(hand);

        if (!stack.isEmpty() && stack.onEntitySwing((LivingEntity) (Object) this))
            ci.cancel();
    }

    @Inject(method = "swapHandItems", at = @At("HEAD"), cancellable = true)
    private void kilt$callSwapHandItemsEvent(CallbackInfo ci, @Share("event") LocalRef<LivingSwapItemsEvent.Hands> event) {
        event.set(CommonHooks.onLivingSwapHandItems((LivingEntity) (Object) this));

        if (event.get().isCanceled())
            ci.cancel();
    }

    @WrapOperation(method = "swapHandItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void kilt$changeHandItems(LivingEntity instance, EquipmentSlot slot, ItemStack itemStack, Operation<Void> original, @Share("event") LocalRef<LivingSwapItemsEvent.Hands> eventRef) {
        var event = eventRef.get();

        if (slot == EquipmentSlot.OFFHAND && !event.getItemSwappedToOffHand().equals(itemStack)) {
            itemStack = event.getItemSwappedToOffHand();
        } else if (slot == EquipmentSlot.MAINHAND && !event.getItemSwappedToMainHand().equals(itemStack)) {
            itemStack = event.getItemSwappedToMainHand();
        }

        original.call(instance, slot, itemStack);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void kilt$callJumpEvent(CallbackInfo ci) {
        CommonHooks.onLivingJump((LivingEntity) (Object) this);
    }

    @Definition(id = "add", method = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;")
    @Expression("?.add(?, @(?), ?)")
    @ModifyExpressionValue(method = "jumpInLiquid", at = @At("MIXINEXTRAS:EXPRESSION"))
    private double kilt$adjustWithSwimSpeed(double original) {
        return original * this.getAttributeValue(NeoForgeMod.SWIM_SPEED);
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z", ordinal = 0))
    private boolean kilt$checkIsInNeoFluidType(LivingEntity instance, Operation<Boolean> original, @Local FluidState fluidState) {
        return original.call(instance) || (instance.isInFluidType(fluidState) && fluidState.neo$getFluidType() != NeoForgeMod.LAVA_TYPE.value());
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0), ordinal = 1)
    private float kilt$adjustMovementToSwimSpeed(float original) {
        return original * (float) this.getAttributeValue(NeoForgeMod.SWIM_SPEED);
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float kilt$tryUseNeoFriction(Block instance, Operation<Float> original) {
        var pos = this.getBlockPosBelowThatAffectsMyMovement();
        var state = this.level().getBlockState(pos);
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(state.getBlock().getClass(), IBlockExtension.class, "getFriction", float.class, BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return state.getFriction(this.level(), pos, this);
        }

        return original.call(instance);
    }

    @Definition(id = "getInBlockState", method = "Lnet/minecraft/world/entity/LivingEntity;getInBlockState()Lnet/minecraft/world/level/block/state/BlockState;")
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "SCAFFOLDING", field = "Lnet/minecraft/world/level/block/Blocks;SCAFFOLDING:Lnet/minecraft/world/level/block/Block;")
    @Expression("this.getInBlockState().is(SCAFFOLDING)")
    @WrapOperation(method = "handleOnClimbable", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsScaffolding(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.isScaffolding((LivingEntity) (Object) this);
    }

    @WrapOperation(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;equipmentHasChanged(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$callEquipmentChangeEvent(LivingEntity instance, ItemStack oldItem, ItemStack newItem, Operation<Boolean> original, @Local EquipmentSlot slot) {
        var result = original.call(instance, oldItem, newItem);
        if (result) {
            NeoForge.EVENT_BUS.post(new LivingEquipmentChangeEvent((LivingEntity) (Object) this, slot, oldItem, newItem));
        }

        return result;
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z", ordinal = 0))
    private void kilt$storeFluidType(CallbackInfo ci, @Share("fluidType") LocalRef<FluidType> fluidTypeRef) {
        fluidTypeRef.set(this.getMaxHeightFluidType());
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D", ordinal = 1))
    private double kilt$tryUseFluidTypeHeight(LivingEntity instance, TagKey tagKey, Operation<Double> original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef) {
        var fluidType = fluidTypeRef.get();

        if (!fluidType.isAir()) {
            return instance.getFluidTypeHeight(fluidType);
        }

        return original.call(instance, tagKey);
    }

    @Definition(id = "getFluidJumpThreshold", method = "Lnet/minecraft/world/entity/LivingEntity;getFluidJumpThreshold()D")
    @Expression("? = this.getFluidJumpThreshold()")
    @Inject(method = "aiStep", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$storeFluidHeightAndJumpThresholds(CallbackInfo ci, @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Share("fluidHeight") LocalDoubleRef fluidHeightRef, @Share("fluidJumpThreshold") LocalDoubleRef fluidJumpThresholdRef) {
        fluidHeightRef.set(d3);
        fluidJumpThresholdRef.set(d4);
    }

    // like this
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", ordinal = 2))
    private void kilt$handleFluidTypeJump(CallbackInfo ci, @Share("fluidType") LocalRef<FluidType> fluidTypeRef, @Share("fluidHeight") LocalDoubleRef fluidHeightRef, @Share("fluidJumpThreshold") LocalDoubleRef fluidJumpThresholdRef) {
        var fluidType = fluidTypeRef.get();
        if (fluidType == null)
            fluidType = this.getMaxHeightFluidType();

        if (!(fluidType.isAir() || this.onGround() && !(fluidHeightRef.get() > fluidJumpThresholdRef.get()))) {
            this.jumpInFluid(fluidType);
        }
    }

    @Definition(id = "noJumpDelay", field = "Lnet/minecraft/world/entity/LivingEntity;noJumpDelay:I")
    @Expression("this.noJumpDelay == 0")
    @ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$preventJumpGroundIfInFluid(boolean original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef, @Share("fluidHeight") LocalDoubleRef fluidHeightRef, @Share("fluidJumpThreshold") LocalDoubleRef fluidJumpThresholdRef) {
        var fluidType = fluidTypeRef.get();
        if (fluidType == null)
            fluidType = this.getMaxHeightFluidType();

        if (fluidType.isAir() || this.onGround() && !(fluidHeightRef.get() > fluidJumpThresholdRef.get())) {
            return original;
        }

        return false;
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpInLiquid(Lnet/minecraft/tags/TagKey;)V"))
    private void kilt$checkIfFluidTypeIsActuallyType(LivingEntity instance, TagKey<Fluid> fluidTag, Operation<Void> original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef) {
        if (fluidTag == FluidTags.WATER) {
            if (instance.getFluidHeight(FluidTags.WATER) > 0) {
                original.call(instance, fluidTag);
            } else {
                instance.jumpInFluid(NeoForgeMod.WATER_TYPE.value());
            }
        } else if (fluidTag == FluidTags.LAVA) {
            if (instance.getFluidHeight(FluidTags.LAVA) > 0) {
                original.call(instance, fluidTag);
            } else {
                instance.jumpInFluid(NeoForgeMod.LAVA_TYPE.value());
            }
        } else {
            original.call(instance, fluidTag);
        }
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "ELYTRA", field = "Lnet/minecraft/world/item/Items;ELYTRA:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(ELYTRA)")
    @WrapOperation(method = "updateFallFlying", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanElytraFly(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canElytraFly((LivingEntity) (Object) this);
    }

    @WrapOperation(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$tryHandleFly(ItemStack elytraStack, Operation<Boolean> original, @Local LocalBooleanRef flag) {
        if (elytraStack.getItem() != Items.ELYTRA && elytraStack.canElytraFly((LivingEntity) (Object) this)) {
            flag.set(elytraStack.elytraFlightTick((LivingEntity) (Object) this, this.fallFlyTicks));
            return false;
        }

        return original.call(elytraStack);
    }

    @ModifyExpressionValue(method = "updatingUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
    private ItemStack kilt$tryUseContinueHook(ItemStack original) {
        if (CommonHooks.canContinueUsing(this.useItem, original)) {
            this.useItem = original;
        }

        return original;
    }

    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    private void kilt$callItemUseTickEvent(ItemStack usingItem, CallbackInfo ci) {
        if (!usingItem.isEmpty())
            this.useItemRemaining = EventHooks.onItemUseTick((LivingEntity) (Object) this, usingItem, this.getUseItemRemainingTicks());
    }

    @WrapWithCondition(method = "updateUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    private boolean kilt$checkStillHasRemainingTicks(ItemStack instance, Level level, LivingEntity livingEntity, int count) {
        return this.getUseItemRemainingTicks() > 0;
    }

    @WrapOperation(method = "updateUsingItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I", opcode = Opcodes.GETFIELD))
    private int kilt$clampIfBelowZero(LivingEntity instance, Operation<Integer> original) {
        int value = original.call(instance);

        return Math.max(value, 0);
    }

    @Inject(method = "startUsingItem", at = @At("HEAD"))
    private void kilt$storeCurrentUseItem(InteractionHand hand, CallbackInfo ci, @Share("useItem") LocalRef<ItemStack> useItemRef) {
        useItemRef.set(this.useItem);
    }

    @WrapOperation(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int kilt$tryStartUsingItem(ItemStack instance, LivingEntity entity, Operation<Integer> original, @Cancellable CallbackInfo ci, @Local(argsOnly = true) InteractionHand hand, @Share("useItem") LocalRef<ItemStack> useItemRef) {
        int duration = EventHooks.onItemUseStart(entity, instance, hand, original.call(instance, entity));

        if (duration < 0) {
            ci.cancel();
            this.useItem = useItemRef.get();
            return 0;
        }

        return duration;
    }

    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$tryFinishUsingItem(ItemStack instance, Level level, LivingEntity livingEntity, Operation<ItemStack> original) {
        return EventHooks.onItemUseFinish(livingEntity, instance.copy(), livingEntity.getUseItemRemainingTicks(), original.call(instance, level, livingEntity));
    }

    @WrapOperation(method = "releaseUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    private void kilt$checkShouldStopUsingItem(ItemStack instance, Level level, LivingEntity livingEntity, int timeLeft, Operation<Void> original) {
        if (!EventHooks.onUseItemStop(livingEntity, instance, timeLeft)) {
            ItemStack copy = livingEntity instanceof Player ? instance.copy() : null;
            original.call(instance, level, livingEntity, timeLeft);

            if (copy != null && instance.isEmpty()) {
                EventHooks.onPlayerDestroyItem((Player) livingEntity, copy, livingEntity.getUsedItemHand());
            }
        }
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void kilt$tryStopUsingItem(CallbackInfo ci) {
        if (this.isUsingItem() && !this.useItem.isEmpty())
            this.useItem.onStopUsing((LivingEntity) (Object) this, this.useItemRemaining);
    }

    @Definition(id = "item", local = @Local(type = Item.class))
    @Definition(id = "getUseAnimation", method = "Lnet/minecraft/world/item/Item;getUseAnimation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/UseAnim;")
    @Definition(id = "BLOCK", field = "Lnet/minecraft/world/item/UseAnim;BLOCK:Lnet/minecraft/world/item/UseAnim;")
    @Expression("item.getUseAnimation(?) != BLOCK")
    @ModifyExpressionValue(method = "isBlocking", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanPerformAction(boolean original) {
        return original && !this.useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK);
    }

    // The rest of the bed checks are implemented via Fabric API.

    @ModifyReturnValue(method = "checkBedExists", at = @At("RETURN"))
    private boolean kilt$checkBedExists(boolean hasBed) {
        return EventHooks.canEntityContinueSleeping((LivingEntity) (Object) this, hasBed ? null : Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
    }

    @ModifyReturnValue(method = "getProjectile", at = @At("RETURN"))
    private ItemStack kilt$tryGetProjectileStack(ItemStack original, @Local(argsOnly = true) ItemStack weapon) {
        return CommonHooks.getProjectile((LivingEntity) (Object) this, weapon, original);
    }

    @WrapOperation(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object kilt$tryGetFoodProperties(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "getFoodProperties", ItemStack.class, LivingEntity.class)) {
            return instance.getFoodProperties((LivingEntity) (Object) this);
        }

        return original.call(instance, dataComponentType);
    }

    @Override
    public boolean removeEffectsCuredBy(EffectCure cure) {
        if (this.level().isClientSide)
            return false;

        boolean ret = false;
        Iterator<MobEffectInstance> itr = this.activeEffects.values().iterator();

        while (itr.hasNext()) {
            MobEffectInstance effect = itr.next();

            if (effect.neoforge$getCures().contains(cure) && !EventHooks.onEffectRemoved((LivingEntity) (Object) this, effect, cure)) {
                this.onEffectRemoved(effect);
                itr.remove();
                ret = true;
                this.effectsDirty = true;
            }
        }

        return ret;
    }

    @Override
    public boolean shouldRiderFaceForward(Player player) {
        return (Object) this instanceof Pig;
    }

    @Inject(method = "getEquipmentSlotForItem", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseModdedSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        EquipmentSlot slot = stack.getEquipmentSlot();

        if (slot != null)
            cir.setReturnValue(slot);
    }

    @ModifyReturnValue(method = "canDisableShield", at = @At("RETURN"))
    private boolean kilt$checkCanDisableShield(boolean original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getMainHandItem().getItem().getClass(), Item.class, "canDisableShield", ItemStack.class, ItemStack.class, LivingEntity.class, LivingEntity.class)) {
            return this.getMainHandItem().canDisableShield(this.useItem, (LivingEntity) (Object) this, (LivingEntity) (Object) this);
        }

        return original;
    }
}
