// TRACKED HASH: 34fb617752c8022973f9dca4fb9eed32600931bf
package xyz.bluspring.kilt.injects.world.entity;

import com.bawnorton.mixinsquared.TargetHandler;
import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Mixin(value = Entity.class, priority = 1100)
@Extends(AttachmentHolder.class)
public abstract class EntityInject implements IEntityExtension, EntityInjection {
    @Shadow public Level level;
    @Shadow public abstract float getBbWidth();
    @Shadow public abstract float getBbHeight();
    @Shadow protected abstract void unsetRemoved();
    @Shadow private EntityDimensions dimensions;
    @Shadow public abstract Level level();
    @Shadow protected abstract void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState);
    @Shadow protected abstract void playMuffledStepSound(BlockState state);
    @Shadow private @Nullable Entity vehicle;
    @Shadow public abstract EntityType<?> getType();
    @Shadow @Final private EntityType<?> type;
    @Shadow public abstract boolean isRemoved();
    @Shadow public abstract void discard();
    @Shadow public abstract @Nullable Entity getVehicle();
    @Shadow public float fallDistance;
    @Shadow public abstract void clearFire();
    @Shadow protected boolean wasTouchingWater;
    @Shadow public abstract boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluidTag, double motionScale);
    @Shadow public abstract Vec3 getDeltaMovement();
    @Shadow public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow public abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    public abstract RegistryAccess registryAccess();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityDimensions;eyeHeight()F"))
    private float kilt$useSizesFromEvent(EntityDimensions instance, Operation<Float> original) {
        var event = EventHooks.getEntitySizeForge((Entity) (Object) this, Pose.STANDING, dimensions, instance);

        this.dimensions = event.getNewSize();
        return original.call(this.dimensions);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callForgeEntityInitEvents(EntityType<?> entityType, Level level, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityEvent.EntityConstructing((Entity) (Object) this));
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "CONSTANT", args = "floatValue=0.5"))
    private float kilt$useFluidFallDistanceModifier(float original) {
        var modifier = this.getFluidFallDistanceModifier(NeoForgeMod.LAVA_TYPE.value());
        if (original == 0.5)
            return modifier;

        return original; // Kilt: prioritize using other mods' modifications if possible.
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWaterRainOrBubble()Z"))
    private boolean kilt$checkExtinguishingFluidType(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType));
    }

    @WrapOperation(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkVerticalCollision(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local BlockPos pos) {
        return original.call(instance, tagKey) || instance.collisionExtendsVertically(this.level(), pos, (Entity) (Object) this);
    }

    // TODO: do we need to implement step height?

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

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;clear()V", shift = At.Shift.AFTER, remap = false))
    private void kilt$clearForgeFluidHeight(CallbackInfoReturnable<Boolean> cir) {
        this.forgeFluidTypeHeight.clear();
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateInWaterStateAndDoWaterCurrentPushing()V", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$tryHandleForgeFluids(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.getVehicle() instanceof Boat)) {
            var fluidDistanceModifier = this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().filter(e -> !e.getKey().isAir() && !e.getKey().isVanilla())
                .map(e -> this.getFluidFallDistanceModifier(e.getKey()))
                .min(Float::compare);
            if (fluidDistanceModifier.isPresent()) {
                this.fallDistance *= fluidDistanceModifier.orElseThrow();

                if (this.isInFluidType((fluidType, height) -> !fluidType.isAir() && !fluidType.isVanilla() && this.canFluidExtinguish(fluidType))) {
                    this.clearFire();
                }

                cir.setReturnValue(this.isInFluidType());
            }
        }
    }

    @WrapOperation(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z"))
    private boolean kilt$checkForgeFluidHeight(Entity instance, TagKey<Fluid> fluidTag, double motionScale, Operation<Boolean> original, @Share("updateFluidHeight") LocalRef<BooleanSupplier> updateFluidHeightRef) {
        if (updateFluidHeightRef.get() != null) {
            return updateFluidHeightRef.get().getAsBoolean();
        }

        return original.call(instance, fluidTag, motionScale);
    }

    @Inject(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$clearForgeFluidTypeOnEyes(CallbackInfo ci) {
        this.forgeFluidTypeOnEyes = NeoForgeMod.EMPTY_TYPE.value();
    }

    @Inject(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$updateForgeFluidTypeOnEyes(CallbackInfo ci, @Local FluidState fluidState) {
        this.forgeFluidTypeOnEyes = fluidState.getFluidType();
    }

    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
    private boolean kilt$checkIsInFluidType(boolean original) {
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
        return original.setSourcePos(pos);
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
    private void kilt$addSerializedCapabilityData(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag attachments = ((AttachmentHolder) (Object) (this)).serializeAttachments(registryAccess());
        if (attachments != null) compound.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, attachments);

        // Kilt: we don't need to implement persistent data, as that is already handled by Porting Lib.
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z", ordinal = 1))
    private void kilt$loadSerializedForgeData(CompoundTag compound, CallbackInfo ci) {
        // Kilt: we don't need to implement persistent data, as that is already handled by Porting Lib.

        if (compound.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND))
            ((AttachmentHolder) (Object) (this)).deserializeAttachments(registryAccess(), compound.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY));
    }

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean kilt$tryCaptureDrops(Level instance, Entity entity, Operation<Boolean> original) {
        if (this.captureDrops() != null && entity instanceof ItemEntity itemEntity) {
            this.captureDrops().add(itemEntity);
            return false;
        } else {
            return original.call(instance, entity);
        }
    }

    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void fireTickEvents(Entity instance, Operation<Void> original) {
        // Neo: Permit cancellation of Entity#tick via EntityTickEvent.Pre
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

    @Inject(method = "removeVehicle", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanMountEntity(CallbackInfo ci) {
        if (this.vehicle != null && !EventHooks.canMountEntity((Entity) (Object) this, this.vehicle, false))
            ci.cancel();
    }

    @ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
    private boolean kilt$checkIsInFluidTypeForCrawling(boolean original) {
        return original && !this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType));
    }

    @ModifyExpressionValue(method = "thunderHit", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
    private float kilt$checkLightningDamage(float original, @Local(argsOnly = true) LightningBolt lightningBolt) {
        if (original != 5.0f) { // Kilt: prioritize other mods' mixins
            lightningBolt.setDamage(original);
            return original;
        }

        return lightningBolt.getDamage();
    }

    @ModifyReceiver(method = "getTypeName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getDescription()Lnet/minecraft/network/chat/Component;"))
    private EntityType<?> kilt$useTypeGetter(EntityType<?> instance) {
        if (this.type != instance) // Kilt: prioritize other mods' mixins
            return instance;

        return this.getType();
    }

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void kilt$handleChangeDimensionEvent(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        if (!CommonHooks.onTravelToDimension((Entity) (Object) this, transition.newLevel().dimension())) {
            cir.setReturnValue(null);
        }
    }

    @WrapOperation(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions kilt$callForgeSizeEvent(Entity instance, Pose pose, Operation<EntityDimensions> original, @Local EntityDimensions entityDimensions) {
        EntityEvent.Size sizeEvent = EventHooks.getEntitySizeForge(instance, pose, entityDimensions, original.call(instance, pose));
        return sizeEvent.getNewSize();
    }

    // Kilt: Little workaround to allow mixins to still occur in updateFluidHeightAndDoFluidPushing
    // I swear to god if this method is accessed in a non-thread safe way, I will scream
    @Unique private Predicate<FluidState> kilt$shouldUpdateFluid;

    @Override
    public void updateFluidHeightAndDoFluidPushing() {
        this.updateFluidHeightAndDoFluidPushing(Predicates.alwaysTrue());
    }

    @Override
    public void updateFluidHeightAndDoFluidPushing(Predicate<FluidState> shouldUpdate) {
        this.kilt$shouldUpdateFluid = shouldUpdate;
        this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 1.0);
        this.kilt$shouldUpdateFluid = null;
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;<init>()V", shift = At.Shift.AFTER))
    private void kilt$initInterimCalcs(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs) {
        interimCalcs.set(new Object2ObjectArrayMap<>(FluidType.SIZE.get() - 1));
    }

    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkIsValidFluidType(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef) {
        fluidTypeRef.set(instance.getFluidType());

        if (this.kilt$shouldUpdateFluid != null) {
            return !fluidTypeRef.get().isAir() && this.kilt$shouldUpdateFluid.test(instance);
        }

        return original.call(instance, tag) || !fluidTypeRef.get().isAir();
    }

    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private double kilt$useInterimCalc(double a, double b, Operation<Double> original, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs, @Share("fluidType") LocalRef<FluidType> fluidTypeRef, @Share("interim") LocalRef<MutableTriple<Double, Vec3, Integer>> interim) {
        interim.set(interimCalcs.get().computeIfAbsent(fluidTypeRef.get(), t -> MutableTriple.of(0.0, Vec3.ZERO, 0)));
        interim.get().setLeft(Math.max(a, interim.get().getLeft()));

        return original.call(a, b);
    }

    // TODO: there is definitely a better way to do this.
    @Definition(id = "bl", local = @Local(type = boolean.class, ordinal = 0))
    @Expression("bl")
    @ModifyExpressionValue(method = "updateFluidHeightAndDoFluidPushing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsPushedByFluid(boolean original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef) {
        return this.isPushedByFluid(fluidTypeRef.get()) || original;
    }

    // Kilt: we don't need to handle d0 < 0.4D, that's already updated

    @WrapOperation(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private Vec3 kilt$updateInterimValues(Vec3 instance, Vec3 vec, Operation<Vec3> original, @Share("interim") LocalRef<MutableTriple<Double, Vec3, Integer>> interim) {
        interim.get().setMiddle(interim.get().getMiddle().add(vec));
        interim.get().setRight(interim.get().getRight() + 1);

        return original.call(instance, vec);
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D", ordinal = 0), cancellable = true)
    private void kilt$useInterimValuesForCalc(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs) {
        if (interimCalcs.get().isEmpty() || (interimCalcs.get().size() == 1 && (interimCalcs.get().containsKey(NeoForgeMod.WATER_TYPE.value()) || interimCalcs.get().containsKey(NeoForgeMod.LAVA_TYPE.value())))) {
            interimCalcs.get().forEach((fluidType, interim) -> {
                this.setFluidTypeHeight(fluidType, interim.getLeft());
            });

            return;
        }

        // Kilt: we have to reimplement everything, cuz we can't wrap blocks of code into a loop.
        interimCalcs.get().forEach((fluidType, interim) -> {
            if (interim.getMiddle().length() > 0) {
                if (interim.getRight() > 0) {
                    interim.setMiddle(interim.getMiddle().scale(1.0 / (double) interim.getRight()));
                }

                if (!((Object) this instanceof Player)) {
                    interim.setMiddle(interim.getMiddle().normalize());
                }

                Vec3 velocity = this.getDeltaMovement();
                interim.setMiddle(interim.getMiddle().scale(this.getFluidMotionScale(fluidType)));

                double tolerance = 0.003;
                if (Math.abs(velocity.x) < tolerance && Math.abs(velocity.z) < tolerance && interim.getMiddle().length() < 0.0045000000000000005) {
                    interim.setMiddle(interim.getMiddle().normalize().scale(0.0045000000000000005));
                }

                this.setDeltaMovement(this.getDeltaMovement().add(interim.getMiddle()));
            }

            this.setFluidTypeHeight(fluidType, interim.getLeft());
        });

        if (fluidTag == FluidTags.WATER)
            cir.setReturnValue(this.isInFluidType(NeoForgeMod.WATER_TYPE.value()));
        else if (fluidTag == FluidTags.LAVA)
            cir.setReturnValue(this.isInFluidType(NeoForgeMod.LAVA_TYPE.value()));
        else
            cir.setReturnValue(false);
    }

    @WrapWithCondition(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;put(Ljava/lang/Object;D)D", remap = false))
    private boolean kilt$ensureIsActuallyInTag(Object2DoubleMap instance, Object o, double v, @Share("fluidType") LocalRef<FluidType> fluidTypeRef, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs, @Local(argsOnly = true) TagKey<Fluid> fluidTag) {
        if (fluidTypeRef.get() == null && interimCalcs.get() == null)
            return true;

        if (fluidTag == FluidTags.WATER) {
            if (fluidTypeRef.get() != null && !fluidTypeRef.get().isAir())
                return fluidTypeRef.get() == NeoForgeMod.WATER_TYPE.value() || fluidTypeRef.get() == PortingLibFluids.WATER_TYPE;
            else if (interimCalcs.get() != null)
                return interimCalcs.get().containsKey(NeoForgeMod.WATER_TYPE.value());
        } else if (fluidTag == FluidTags.LAVA) {
            if (fluidTypeRef.get() != null && !fluidTypeRef.get().isAir())
                return fluidTypeRef.get() == NeoForgeMod.LAVA_TYPE.value() || fluidTypeRef.get() == PortingLibFluids.LAVA_TYPE;
            else if (interimCalcs.get() != null)
                return interimCalcs.get().containsKey(NeoForgeMod.LAVA_TYPE.value());
        }

        return true;
    }

    @ModifyReturnValue(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "RETURN", ordinal = 1))
    private boolean kilt$ensureIsActuallyInFluidType(boolean original, @Share("fluidType") LocalRef<FluidType> fluidTypeRef, @Share("interimCalcs") LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs, @Local(argsOnly = true) TagKey<Fluid> fluidTag) {
        if (fluidTag == FluidTags.WATER) {
            if (fluidTypeRef.get() != null && !fluidTypeRef.get().isAir())
                return fluidTypeRef.get() == NeoForgeMod.WATER_TYPE.value() || fluidTypeRef.get() == PortingLibFluids.WATER_TYPE;
            else if (interimCalcs.get() != null)
                return interimCalcs.get().containsKey(NeoForgeMod.WATER_TYPE.value());
        } else if (fluidTag == FluidTags.LAVA) {
            if (fluidTypeRef.get() != null && !fluidTypeRef.get().isAir())
                return fluidTypeRef.get() == NeoForgeMod.LAVA_TYPE.value() || fluidTypeRef.get() == PortingLibFluids.LAVA_TYPE;
            else if (interimCalcs.get() != null)
                return interimCalcs.get().containsKey(NeoForgeMod.LAVA_TYPE.value());
        }

        return original;
    }

    @Inject(method = "setPosRaw", at = @At("TAIL"))
    private void kilt$ensureChunkLoaded(double x, double y, double z, CallbackInfo ci) {
        if (this.isAddedToLevel() && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().getChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4);
        }
    }

    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight = new Object2DoubleArrayMap<>(FluidType.SIZE.get());
    private FluidType forgeFluidTypeOnEyes = NeoForgeMod.EMPTY_TYPE.value();

    @Override
    public CompoundTag getPersistentData() {
        return ((io.github.fabricators_of_create.porting_lib.entity.injects.EntityInjection) this).getCustomData();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return this.level.random.nextFloat() < fallDistance - 0.5F
                && ((Object) this) instanceof LivingEntity
                && (((Object) this) instanceof Player || EventHooks.canEntityGrief(this.level, ((Entity) (Object) this)))
                && this.getBbWidth() * this.getBbWidth() * this.getBbHeight() > 0.512F;
    }

    private boolean isAddedToLevel;

    @Override
    public boolean isAddedToLevel() {
        return isAddedToLevel;
    }

    @Override
    public void onAddedToLevel() {
        isAddedToLevel = true;
    }

    @Override
    public void onRemovedFromLevel() {
        isAddedToLevel = false;
    }

    @Override
    public void revive() {
        this.unsetRemoved();
    }

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

        var stream = this.forgeFluidTypeHeight.object2DoubleEntrySet().stream();
        return forAllTypes ? stream.allMatch(e -> predicate.test(e.getKey(), e.getDoubleValue()))
            : stream.anyMatch(e -> predicate.test(e.getKey(), e.getDoubleValue()));
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

        return this.forgeFluidTypeHeight.object2DoubleEntrySet()
            .stream()
            .max(Comparator.comparingDouble(Object2DoubleMap.Entry::getDoubleValue))
            .map(Object2DoubleMap.Entry::getKey)
            .orElseGet(NeoForgeMod.EMPTY_TYPE::value);
    }

    public EntityDimensions getDimensionsForge(Pose pose) {
        return getDimensions(pose);
    }
}