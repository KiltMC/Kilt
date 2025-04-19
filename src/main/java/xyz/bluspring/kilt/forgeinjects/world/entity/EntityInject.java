// TRACKED HASH: 34fb617752c8022973f9dca4fb9eed32600931bf
package xyz.bluspring.kilt.forgeinjects.world.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fabricators_of_create.porting_lib.entity.extensions.EntityExtensions;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fluids.FluidType;
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
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;
import xyz.bluspring.kilt.injections.world.entity.LightningBoltInjection;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;

@Mixin(Entity.class)
@Extends(CapabilityProvider.class)
public abstract class EntityInject implements IForgeEntity, CapabilityProviderInjection, ICapabilityProviderImpl<Entity>, EntityExtensions, EntityInjection {
    @Shadow public Level level;
    @Shadow public abstract float getBbWidth();
    @Shadow public abstract float getBbHeight();
    @Shadow protected abstract void unsetRemoved();
    @Shadow protected abstract float getEyeHeight(Pose pose, EntityDimensions dimensions);

    @Shadow private EntityDimensions dimensions;

    @Shadow public abstract Level level();

    @Shadow protected abstract void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState);

    @Shadow protected abstract void playMuffledStepSound(BlockState state);

    @Shadow private @Nullable Entity vehicle;

    @Shadow public abstract EntityType<?> getType();

    @Shadow @Final private EntityType<?> type;

    @Shadow public abstract boolean isRemoved();

    @Shadow public abstract void discard();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F"))
    private float kilt$useSizesFromEvent(Entity instance, Pose pose, EntityDimensions dimensions, Operation<Float> original) {
        var event = ForgeEventFactory.getEntitySizeForge(instance, pose, dimensions, original.call(instance, pose, dimensions));

        this.dimensions = event.getNewSize();
        return event.getNewEyeHeight();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callForgeEntityInitEvents(EntityType<?> entityType, Level level, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityEvent.EntityConstructing((Entity) (Object) this));
        this.gatherCapabilities();
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void kilt$invalidateEntityCapabilities(Entity.RemovalReason reason, CallbackInfo ci) {
        this.invalidateCaps();
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "CONSTANT", args = "floatValue=0.5"))
    private float kilt$useFluidFallDistanceModifier(float original) {
        var modifier = this.getFluidFallDistanceModifier(ForgeMod.LAVA_TYPE.get());
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

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;clear()V", shift = At.Shift.AFTER))
    private void kilt$clearForgeFluidHeight(CallbackInfoReturnable<Boolean> cir) {
        this.forgeFluidTypeHeight.clear();
    }

    // TODO: implement updateInWaterStateAndDoFluidPushing

    @Inject(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$clearForgeFluidTypeOnEyes(CallbackInfo ci) {
        this.forgeFluidTypeOnEyes = ForgeMod.EMPTY_TYPE.get();
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
            cir.setReturnValue(this.isEyeInFluidType(ForgeMod.WATER_TYPE.get()));
        else if (fluidTag == FluidTags.LAVA)
            cir.setReturnValue(this.isEyeInFluidType(ForgeMod.LAVA_TYPE.get()));
    }

    @Definition(id = "fluidHeight", field = "Lnet/minecraft/world/entity/Entity;fluidHeight:Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;")
    @Definition(id = "getDouble", method = "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;getDouble(Ljava/lang/Object;)D")
    @Definition(id = "LAVA", field = "Lnet/minecraft/tags/FluidTags;LAVA:Lnet/minecraft/tags/TagKey;")
    @Expression("this.fluidHeight.getDouble(LAVA) > 0.0")
    @ModifyExpressionValue(method = "isInLava", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkForgeFluidTypeHeight(boolean original) {
        return original || this.forgeFluidTypeHeight.getDouble(ForgeMod.LAVA_TYPE.get()) > 0.0D;
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
    private void kilt$addCanUpdateTag(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        compound.putBoolean("CanUpdate", this.canUpdate);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void kilt$addSerializedCapabilityData(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        var capabilities = this.serializeCaps();
        if (capabilities != null)
            compound.put("ForgeCaps", capabilities);

        // Kilt: we don't need to implement persistent data, as that is already handled by Porting Lib.
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z", ordinal = 1))
    private void kilt$loadSerializedForgeData(CompoundTag compound, CallbackInfo ci) {
        // Kilt: we don't need to implement persistent data, as that is already handled by Porting Lib.

        if (compound.contains("CanUpdate", Tag.TAG_ANY_NUMERIC))
            this.canUpdate(compound.getBoolean("CanUpdate"));

        if (compound.contains("ForgeCaps", Tag.TAG_COMPOUND))
            this.deserializeCaps(compound.getCompound("ForgeCaps"));
    }

    @WrapWithCondition(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private boolean kilt$checkCanUpdate(Entity instance) {
        return instance.canUpdate();
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRide(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
    private void kilt$checkCanMountEntity(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeEventFactory.canMountEntity((Entity) (Object) this, vehicle, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanMountEntity(CallbackInfo ci) {
        if (this.vehicle != null && !ForgeEventFactory.canMountEntity((Entity) (Object) this, this.vehicle, false))
            ci.cancel();
    }

    @ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
    private boolean kilt$checkIsInFluidTypeForCrawling(boolean original) {
        return original && !this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType));
    }

    @ModifyExpressionValue(method = "thunderHit", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
    private float kilt$checkLightningDamage(float original, @Local(argsOnly = true) LightningBolt lightningBolt) {
        if (original == 5.0f) { // Kilt: prioritize other mods' mixins
            return original;
        }

        return ((LightningBoltInjection) lightningBolt).getDamage();
    }

    @ModifyReceiver(method = "getTypeName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getDescription()Lnet/minecraft/network/chat/Component;"))
    private EntityType<?> kilt$useTypeGetter(EntityType<?> instance) {
        if (this.type != instance) // Kilt: prioritize other mods' mixins
            return instance;

        return this.getType();
    }

    @Nullable
    public Entity changeDimension(ServerLevel level, ITeleporter teleporter) {
        // Kilt: redirect to Porting Lib's teleporter
        return this.changeDimension(level, (io.github.fabricators_of_create.porting_lib.entity.ITeleporter) teleporter);
    }

    // TODO: implement getEntitySize

    @Unique
    private boolean canUpdate = true;

    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight = new Object2DoubleArrayMap<>(FluidType.SIZE.get());
    private FluidType forgeFluidTypeOnEyes = ForgeMod.EMPTY_TYPE.get();

    @Override
    public boolean canUpdate() {
        return canUpdate;
    }

    @Override
    public void canUpdate(boolean value) {
        canUpdate = value;
    }

    @Override
    public CompoundTag getPersistentData() {
        return this.getCustomData();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return this.level.random.nextFloat() < fallDistance - .5F
                && ((Object) this) instanceof LivingEntity
                && (((Object) this) instanceof Player || ForgeEventFactory.getMobGriefingEvent(this.level, ((Entity) (Object) this)))
                && this.getBbWidth() * this.getBbWidth() * this.getBbHeight() > .512F;
    }

    private boolean isAddedToWorld;

    @Override
    public boolean isAddedToWorld() {
        return isAddedToWorld;
    }

    @Override
    public void onAddedToWorld() {
        isAddedToWorld = true;
    }

    @Override
    public void onRemovedFromWorld() {
        isAddedToWorld = false;
    }

    @Override
    public void revive() {
        this.unsetRemoved();
        this.reviveCaps();
    }

    public float getEyeHeightAccess(Pose pose, EntityDimensions size) {
        return this.getEyeHeight(pose, size);
    }

    protected final void setFluidTypeHeight(FluidType type, double height) {
        this.forgeFluidTypeHeight.put(type, height);
    }

    @Override
    public double getFluidTypeHeight(FluidType type) {
        return this.forgeFluidTypeHeight.getDouble(type);
    }

    @Override
    public FluidType getMaxHeightFluidType() {
        if (this.forgeFluidTypeHeight.isEmpty())
            return ForgeMod.EMPTY_TYPE.get();

        return this.forgeFluidTypeHeight.object2DoubleEntrySet()
            .stream()
            .max(Comparator.comparingDouble(Object2DoubleMap.Entry::getDoubleValue))
            .map(Object2DoubleMap.Entry::getKey)
            .orElseGet(ForgeMod.EMPTY_TYPE);
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

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean kilt$captureSpawnDrops(Level instance, Entity entity, Operation<Boolean> original) {
        if (captureDrops() != null) {
            captureDrops().add((ItemEntity) entity);
            return false;
        } else {
            return original.call(instance, entity);
        }
    }
}