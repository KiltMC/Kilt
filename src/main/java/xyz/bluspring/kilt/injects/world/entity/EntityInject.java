// TRACKED HASH: 34fb617752c8022973f9dca4fb9eed32600931bf
package xyz.bluspring.kilt.injects.world.entity;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.AttachmentHolderWorkaround;
import xyz.bluspring.kilt.workarounds.InterimCalculation;
import xyz.bluspring.kilt.workarounds.KiltFluidTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(value = Entity.class, priority = 900)
@Extends(AttachmentHolder.class)
public abstract class EntityInject implements IEntityExtension, EntityInjection, AttachmentHolderWorkaround {
    @Shadow protected abstract void unsetRemoved();
    @Shadow private EntityDimensions dimensions;
    @Shadow public abstract Level level();
    @Shadow protected abstract void playMuffledStepSound(BlockState blockState);
    @Shadow protected abstract void playCombinationStepSounds(BlockState blockState, BlockState blockState2);
    @Shadow @Nullable public abstract Entity getVehicle();
    @Shadow public float fallDistance;
    @Shadow public abstract void clearFire();
    @Shadow public abstract RegistryAccess registryAccess();
    @Shadow public abstract float getBbWidth();
    @Shadow public abstract float getBbHeight();
    @Shadow public abstract EntityType<?> getType();
    @Shadow public abstract boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagKey, double d);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V", shift = At.Shift.AFTER))
    private void kilt$setDimensionsFromSizeEvent(EntityType<?> entityType, Level level, CallbackInfo ci, @Share(value = "sizeEvent", namespace = "kilt") LocalRef<EntityEvent.Size> sizeEvent) {
        sizeEvent.set(EventHooks.getEntitySizeForge((Entity) (Object) this, Pose.STANDING, this.dimensions));
        this.dimensions = sizeEvent.get().getNewSize();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callEntityConstructEvent(EntityType<?> entityType, Level level, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityEvent.EntityConstructing((Entity) (Object) this));
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "CONSTANT", args = "floatValue=0.5"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInLava()Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;checkBelowWorld()V")))
    private float kilt$handleLavaFallDistance(float value) {
        if (value != 0.5) // Kilt: Prioritize other mods
            return value;

        return this.getFluidFallDistanceModifier(NeoForgeMod.LAVA_TYPE.value());
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWaterRainOrBubble()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tryCheckInsideBlocks()V")))
    private boolean kilt$checkCanFluidTypeExtinguish(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || instance.isInFluidType((fluidType, height) -> instance.canFluidExtinguish(fluidType));
    }

    @WrapOperation(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkVerticalCollision(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local BlockPos pos) {
        return original.call(instance, tagKey) || instance.collisionExtendsVertically(this.level(), pos, (Entity) (Object) this);
    }

    @Unique private final AtomicReference<BlockPos> kilt$primaryPos = new AtomicReference<>(null);
    @Unique private final AtomicReference<BlockPos> kilt$secondaryPos = new AtomicReference<>(null);

    protected void playCombinationStepSounds(BlockState state, BlockState secondary, BlockPos primaryPos, BlockPos secondaryPos) {
        this.kilt$primaryPos.set(primaryPos);
        this.kilt$secondaryPos.set(secondaryPos);
        this.playCombinationStepSounds(state, secondary);
        this.kilt$primaryPos.set(null);
        this.kilt$secondaryPos.set(null);
    }

    @WrapOperation(method = "playCombinationStepSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$useSoundTypeWithPrimaryPosIfPossible(BlockState instance, Operation<SoundType> original) {
        var primaryPos = this.kilt$primaryPos.getAndSet(null);

        if (primaryPos != null) {
            return instance.getSoundType(this.level(), primaryPos, (Entity) (Object) this);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "playCombinationStepSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playMuffledStepSound(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void kilt$playSoundWithSecondaryPosIfPossible(Entity instance, BlockState state, Operation<Void> original) {
        var secondaryPos = this.kilt$secondaryPos.getAndSet(null);

        if (secondaryPos != null) {
            this.playMuffledStepSound(state, secondaryPos);
            return;
        }

        original.call(instance, state);
    }

    @Unique private final AtomicReference<BlockPos> kilt$stepPos = new AtomicReference<>(null);
    protected void playMuffledStepSound(BlockState state, BlockPos pos) {
        this.kilt$stepPos.set(pos);
        this.playMuffledStepSound(state);
        this.kilt$stepPos.set(null);
    }

    @WrapOperation(method = "playMuffledStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$useSoundTypeWithStepPosIfPossible(BlockState instance, Operation<SoundType> original) {
        var primaryPos = this.kilt$stepPos.getAndSet(null);

        if (primaryPos != null) {
            return instance.getSoundType(this.level(), primaryPos, (Entity) (Object) this);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$useForgeSoundTypeIfPossible(BlockState instance, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(this.level(), pos, (Entity) (Object) this);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWater()Z"))
    private boolean kilt$checkCanSwimInFluidType(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType));
    }

    @WrapOperation(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isUnderWater()Z"))
    private boolean kilt$checkCanStartSwimming(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || this.canStartSwimming();
    }

    @Redirect(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean kilt$forceAlwaysWater(FluidState instance, TagKey<Fluid> tag) {
        return true;
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("HEAD"))
    private void kilt$clearFluidTypeHeights(CallbackInfoReturnable<Boolean> cir) {
        this.forgeFluidTypeHeight.clear();
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z"))
    private void kilt$handleCustomFluidTypes(CallbackInfoReturnable<Boolean> cir) {
        if (this.isInFluidType() && !(this.getVehicle() instanceof Boat)) {
            var entity = (Entity) (Object) this;
            this.fallDistance *= this.forgeFluidTypeHeight.object2DoubleEntrySet().stream()
                .filter(e -> !e.getKey().isAir() && !e.getKey().isVanilla())
                .map(e -> entity.getFluidFallDistanceModifier(e.getKey()))
                .min(Float::compare)
                .orElse(1f);

            if (this.isInFluidType((fluidType, height) -> !fluidType.isAir() && !fluidType.isVanilla() && entity.canFluidExtinguish(fluidType))) {
                this.clearFire();
            }
        }
    }

    @ModifyReturnValue(method = "updateInWaterStateAndDoFluidPushing", at = @At("RETURN"))
    private boolean kilt$checkIsInFluidType(boolean original) {
        return original || this.isInFluidType();
    }

    @Inject(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V", shift = At.Shift.AFTER))
    private void kilt$resetCurrentEyeFluidType(CallbackInfo ci) {
        this.forgeFluidTypeOnEyes = NeoForgeMod.EMPTY_TYPE.value();
    }

    @Inject(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$tryGetFluidTypeForEyes(CallbackInfo ci, @Local FluidState fluidState) {
        this.forgeFluidTypeOnEyes = fluidState.neo$getFluidType();
    }

    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
    private boolean kilt$checkIsInFluidTypeForSprint(boolean original) {
        return original && !this.isInFluidType();
    }

    @WrapOperation(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
    private RenderShape kilt$checkShouldAddRunningEffects(BlockState instance, Operation<RenderShape> original, @Local BlockPos pos) {
        if (instance.addRunningEffects(this.level(), pos, (Entity) (Object) this))
            return RenderShape.INVISIBLE; // Kilt: this effectively cancels the if check.

        return original.call(instance);
    }

    @ModifyExpressionValue(method = "spawnSprintParticle", at = @At(value = "NEW", target = "(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;"))
    private BlockParticleOption kilt$setBlockParticlePos(BlockParticleOption original, @Local(ordinal = 0) BlockPos pos) {
        return original.setPos(pos);
    }

    @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
    private void kilt$checkEyeInFluidType(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if (fluidTag == FluidTags.WATER)
            cir.setReturnValue(this.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()));
        else if (fluidTag == FluidTags.LAVA)
            cir.setReturnValue(this.isEyeInFluidType(NeoForgeMod.LAVA_TYPE.value()));
    }

    @Definition(id = "fluidHeight", field = "Lnet/minecraft/world/entity/Entity;fluidHeight:Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;")
    @Definition(id = "getDouble", method = "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;getDouble(Ljava/lang/Object;)D")
    @Definition(id = "LAVA", field = "Lnet/minecraft/tags/FluidTags;LAVA:Lnet/minecraft/tags/TagKey;")
    @Expression("this.fluidHeight.getDouble(LAVA) > 0.0")
    @ModifyExpressionValue(method = "isInLava", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkForgeFluidTypeHeight(boolean original) {
        return original || this.forgeFluidTypeHeight.getDouble(NeoForgeMod.LAVA_TYPE.value()) > 0.0D;
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void kilt$trySaveCustomNeoData(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag attachments = this.serializeAttachments(this.registryAccess());
        if (attachments != null) {
            compound.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, attachments);
        }

        // Kilt: Persistent data is on Porting Lib.
    }

    @Inject(method = "load", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hasVisualFire:Z", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD))
    private void kilt$tryLoadCustomNeoData(CompoundTag compound, CallbackInfo ci) {
        // Kilt: Persistent data is on Porting Lib.

        if (compound.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, CompoundTag.TAG_COMPOUND)) {
            this.deserializeAttachments(this.registryAccess(), compound.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY));
        }
    }

    // Kilt: Capture drops should be handled by Porting Lib.

    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void kilt$tryTickWithEvent(Entity instance, Operation<Void> original) {
        if (!EventHooks.fireEntityTickPre(instance).isCanceled()) {
            original.call(instance);
            EventHooks.fireEntityTickPost(instance);
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRide(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
    private void kilt$checkCanMountEntity(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (!EventHooks.canMountEntity((Entity) (Object) this, vehicle, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "removeVehicle", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;vehicle:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.PUTFIELD), cancellable = true)
    private void kilt$checkCanDismountEntity(CallbackInfo ci, @Local Entity entity) {
        if (!EventHooks.canMountEntity((Entity) (Object) this, entity, false))
            ci.cancel();
    }

    @ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
    private boolean kilt$checkIsNotInFluidType(boolean original) {
        var entity = (Entity) (Object) this;
        return original && !this.isInFluidType((fluidType, height) -> entity.canSwimInFluidType(fluidType));
    }

    @ModifyExpressionValue(method = "thunderHit", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
    private float kilt$tryUseLightningDamage(float original, @Local(argsOnly = true) LightningBolt lightningBolt) {
        if (original != 5.0f) // Kilt: Prioritize other mods.
            return original;

        return lightningBolt.getDamage();
    }

    @ModifyReceiver(method = "getTypeName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getDescription()Lnet/minecraft/network/chat/Component;"))
    private EntityType kilt$tryUseCustomEntityType(EntityType instance) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Entity.class, "getType")) {
            return this.getType();
        }

        return instance;
    }

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean kilt$checkIsEntityInvulnerable(boolean original, @Local(argsOnly = true) DamageSource damageSource) {
        return CommonHooks.isEntityInvulnerableTo((Entity) (Object) this, damageSource, original);
    }

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanTravelToDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        if (!CommonHooks.onTravelToDimension((Entity) (Object) this, transition.newLevel().dimension()))
            cir.setReturnValue(null);
    }

    @WrapOperation(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions kilt$tryUseCustomEntitySize(Entity instance, Pose pose, Operation<EntityDimensions> original, @Local EntityDimensions oldDimensions) {
        var newDimensions = original.call(instance, pose);
        return EventHooks.getEntitySizeForge(instance, pose, oldDimensions, newDimensions).getNewSize();
    }

    // Kilt: hell.

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateInWaterStateAndDoWaterCurrentPushing()V", shift = At.Shift.AFTER))
    private void kilt$handleNonVanillaFluidPushing(CallbackInfoReturnable<Boolean> cir) {
        this.updateFluidHeightAndDoFluidPushing(KiltFluidTags.EMPTY_NONVANILLA, 1.0);
    }

    public void updateFluidHeightAndDoFluidPushing() {
        this.updateFluidHeightAndDoFluidPushing(KiltFluidTags.EMPTY, 1.0);
    }

    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean kilt$checkIsFluidTypeAir(FluidState instance, TagKey<Fluid> tagKey, Operation<Boolean> original) {
        if (KiltFluidTags.isTagForFluidTypePushing(tagKey)) {
            return !instance.neo$getFluidType().isAir();
        }

        return original.call(instance, tagKey);
    }

    @Definition(id = "bl2", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("bl2 = @(true)")
    @ModifyExpressionValue(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$avoidSettingFlagIfNotActuallyFluidState(int original, @Local FluidState fluidState, @Local(argsOnly = true) TagKey<Fluid> fluidTag, @Local(ordinal = 1) boolean flag) {
        if (!flag && !fluidState.is(fluidTag)) {
            return 0;
        }

        return original;
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private void kilt$storeFluidType(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Local FluidState fluidState, @Share(value = "fluidType", namespace = Kilt.MOD_ID) LocalRef<FluidType> fluidTypeRef) {
        fluidTypeRef.set(fluidState.neo$getFluidType());
    }

    @Definition(id = "max", method = "Ljava/lang/Math;max(DD)D")
    @Definition(id = "e", local = @Local(type = double.class, ordinal = 2))
    @Definition(id = "aABB", local = @Local(type = AABB.class))
    @Definition(id = "minY", field = "Lnet/minecraft/world/phys/AABB;minY:D")
    @Expression("max(e - aABB.minY, ?)")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private double kilt$trySetFluidHeightOfInterim(double a, double b, Operation<Double> original,
                                                   @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs,
                                                   @Local FluidState fluidState,
                                                   @Local(argsOnly = true) TagKey<Fluid> fluidTag,
                                                   @Share("interim") LocalRef<InterimCalculation> interimRef) {
        if (!KiltFluidTags.isTagForFluidTypePushing(fluidTag)) { // Kilt: Used as a marker for us to actually apply fluid type stuff.
            return original.call(a, b);
        }

        if (interimCalcs.get() == null) {
            interimCalcs.set(new Object2ObjectArrayMap<>());
        }

        InterimCalculation interim = interimCalcs.get().computeIfAbsent(fluidState.neo$getFluidType(), t -> new InterimCalculation());
        interim.setFluidHeight(original.call(a, interim.getFluidHeight()));
        interimRef.set(interim);

        if (fluidState.is(fluidTag))
            return original.call(a, b);
        else
            return b;
    }

    @Definition(id = "bl", local = @Local(type = boolean.class, ordinal = 0))
    @Expression("bl")
    @ModifyExpressionValue(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsPushedByFluid(boolean original, @Share(value = "fluidType", namespace = Kilt.MOD_ID) LocalRef<FluidType> fluidTypeRef) {
        return this.isPushedByFluid(fluidTypeRef.get()) || original;
    }

    @Definition(id = "d", local = @Local(type = double.class, ordinal = 1))
    @Expression("d < 0.4")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryCheckFluidHeight(double left, double right, Operation<Boolean> original, @Share("interim") LocalRef<InterimCalculation> interimRef) {
        if (interimRef.get() != null) {
            return original.call(interimRef.get().getFluidHeight(), right);
        }

        return original.call(left, right);
    }

    @Definition(id = "vec32", local = @Local(type = Vec3.class, ordinal = 1))
    @Definition(id = "scale", method = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "d", local = @Local(type = double.class, ordinal = 1))
    @Expression("vec32 = @(vec32.scale(d))")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Vec3 kilt$tryScaleWithFluidHeight(Vec3 instance, double d, Operation<Vec3> original, @Share("interim") LocalRef<InterimCalculation> interimRef) {
        if (interimRef.get() != null) {
            return original.call(instance, interimRef.get().getFluidHeight());
        }

        return original.call(instance, d);
    }

    @Definition(id = "vec3", local = @Local(type = Vec3.class, ordinal = 0))
    @Definition(id = "add", method = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "vec32", local = @Local(type = Vec3.class, ordinal = 1))
    @Expression("vec3 = @(vec3.add(vec32))")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Vec3 kilt$adjustFlowVectorWithInterim(Vec3 instance, Vec3 vec3, Operation<Vec3> original, @Share("interim") LocalRef<InterimCalculation> interimRef, @Local FluidState fluidState, @Local(argsOnly = true) TagKey<Fluid> fluidTag, @Local(ordinal = 6) LocalIntRef o) {
        if (interimRef.get() != null) {
            interimRef.get().setFlowVector(original.call(interimRef.get().getFlowVector(), vec3));
            interimRef.get().setBlockCount(interimRef.get().getBlockCount() + 1);
            interimRef.set(null);

            o.set(o.get() - 1); // It's going to get increased, so avoid doing so.
            return instance;
        }

        return original.call(instance, vec3);
    }

    @Definition(id = "vec3", local = @Local(type = Vec3.class, ordinal = 0))
    @Definition(id = "length", method = "Lnet/minecraft/world/phys/Vec3;length()D")
    @Expression("vec3.length() > 0.0")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAnyFlowVectorsMatch(double left, double right, Operation<Boolean> original, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs) {
        var originalResult = original.call(left, right);

        if (interimCalcs.get() != null) {
            for (InterimCalculation interim : interimCalcs.get().values()) {
                if (original.call(interim.getFlowVector().length(), right)) {
                    return true;
                }
            }
        }

        return originalResult;
    }

    @Definition(id = "Player", type = Player.class)
    @Expression("this instanceof Player")
    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleInterimVectorScale(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs) {
        if (interimCalcs.get() != null) {
            for (InterimCalculation interim : interimCalcs.get().values()) {
                if (interim.getBlockCount() > 0) {
                    interim.setFlowVector(interim.getFlowVector().scale(1.0 / (double) interim.getBlockCount()));
                }
            }
        }
    }

    @Definition(id = "vec3", local = @Local(type = Vec3.class, ordinal = 0))
    @Definition(id = "normalize", method = "Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "scale", method = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "vec32", local = @Local(type = Vec3.class, ordinal = 1))
    @Definition(id = "getDeltaMovement", method = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;")
    @Expression("vec3.normalize()")
    @Expression(value = "vec3 = vec3.scale(1.0 / ?)", id = "scale")
    @Expression(value = "vec32 = this.getDeltaMovement()", id = "deltaMovement")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"), slice = @Slice(
        from = @At(value = "MIXINEXTRAS:EXPRESSION", id = "scale"),
        to = @At(value = "MIXINEXTRAS:EXPRESSION", id = "deltaMovement")
    ))
    private Vec3 kilt$handleNormalizeInterims(Vec3 instance, Operation<Vec3> original, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs) {
        if (interimCalcs.get() != null) {
            for (InterimCalculation interim : interimCalcs.get().values()) {
                interim.setFlowVector(original.call(interim.getFlowVector()));
            }
        }

        return original.call(instance);
    }

    @Definition(id = "vec3", local = @Local(type = Vec3.class, ordinal = 0))
    @Definition(id = "scale", method = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "motionScale", local = @Local(type = double.class, ordinal = 0, argsOnly = true))
    @Expression("vec3.scale(motionScale)")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Vec3 kilt$handleInterimScaleBasedOnMotion(Vec3 instance, double d, Operation<Vec3> original, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs) {
        if (interimCalcs.get() != null) {
            var entity = (Entity) (Object) this;

            interimCalcs.get().forEach((fluidType, interim) -> {
                interim.setFlowVector(original.call(interim.getFlowVector(), entity.getFluidMotionScale(fluidType)));
            });
        }

        return original.call(instance, d);
    }

    @Definition(id = "getDeltaMovement", method = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "add", method = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;")
    @Expression("this.getDeltaMovement().add(?)")
    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Vec3 kilt$addVectorFromInterim(Vec3 instance, Vec3 vec, Operation<Vec3> original, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs, @Local(argsOnly = true) TagKey<Fluid> fluidTag) {
        Vec3 current = original.call(instance, vec);
        double scaleFactor = 0.0045000000000000005;

        if (interimCalcs.get() != null) {
            for (FluidType fluidType : interimCalcs.get().keySet()) {
                if (fluidTag != KiltFluidTags.EMPTY_NONVANILLA || !fluidType.isVanilla()) {
                    var interim = interimCalcs.get().get(fluidType);
                    var interimVec = interim.getFlowVector();

                    if (Math.abs(current.x) < 0.003 && Math.abs(current.z) < 0.003 && interimVec.length() < scaleFactor) {
                        interimVec = interimVec.normalize().scale(scaleFactor);
                    }

                    current = current.add(interimVec);
                }
            }
        }

        return current;
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("TAIL"))
    private void kilt$trySetFluidTypeHeights(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, InterimCalculation>> interimCalcs) {
        if (interimCalcs.get() != null) {
            for (Map.Entry<FluidType, InterimCalculation> entry : interimCalcs.get().entrySet()) {
                this.setFluidTypeHeight(entry.getKey(), entry.getValue().getFluidHeight());
            }
        }
    }

    @Override
    public CompoundTag getPersistentData() {
        return this.getCustomData(); // Kilt: Redirect to Porting Lib, why not.
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        var entity = (Entity) (Object) this;
        return this.level().random.nextFloat() < fallDistance - 0.5
            && entity instanceof LivingEntity
            && (entity instanceof Player || EventHooks.canEntityGrief(this.level(), entity))
            && this.getBbWidth() * this.getBbWidth() * this.getBbHeight() > 0.512;
    }

    @Unique private boolean isAddedToLevel;

    @Override
    public final boolean isAddedToLevel() {
        return this.isAddedToLevel;
    }

    @Override
    public void onAddedToLevel() {
        this.isAddedToLevel = true;
    }

    @Override
    public void onRemovedFromLevel() {
        this.isAddedToLevel = false;
    }

    @Override
    public void revive() {
        this.unsetRemoved();
    }

    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight = new Object2DoubleArrayMap<>(FluidType.SIZE.get());
    private FluidType forgeFluidTypeOnEyes = NeoForgeMod.KILT_EMPTY_TYPE_DIRECT;

    protected final void setFluidTypeHeight(FluidType type, double height) {
        this.forgeFluidTypeHeight.put(type, height);
    }

    @Override
    public double getFluidTypeHeight(FluidType type) {
        return this.forgeFluidTypeHeight.getDouble(type);
    }

    @Override
    public boolean isInFluidType(BiPredicate<FluidType, Double> predicate, boolean forAllTypes) {
        if (this.forgeFluidTypeHeight.isEmpty())
            return false;

        return forAllTypes ? this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().allMatch(e -> predicate.test(e.getKey(), e.getDoubleValue()))
            : this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().anyMatch(e -> predicate.test(e.getKey(), e.getDoubleValue()));
    }

    @Override
    public boolean isInFluidType() {
        return !this.forgeFluidTypeHeight.isEmpty();
    }

    @Override
    public FluidType getEyeInFluidType() {
        return this.forgeFluidTypeOnEyes;
    }

    @Override
    public FluidType getMaxHeightFluidType() {
        if (this.forgeFluidTypeHeight.isEmpty())
            return NeoForgeMod.EMPTY_TYPE.value();

        return this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().max(Comparator.comparingDouble(Object2DoubleMap.Entry::getDoubleValue))
            .map(Object2DoubleMap.Entry::getKey)
            .orElseGet(NeoForgeMod.EMPTY_TYPE::value);
    }

    // Kilt TODO: Do we need to implement setData?

    public final void syncData(AttachmentType<?> type) {
        AttachmentSync.syncEntityUpdate((Entity) (Object) this, type);
    }

    @Override
    public <T, C> T getCapability(EntityCapability<T, C> capability, C context) {
        return capability.getCapability((Entity) (Object) this, context);
    }

    @Override
    public <T> T getCapability(EntityCapability<T, @Nullable Void> capability) {
        return capability.getCapability((Entity) (Object) this, null);
    }
}
