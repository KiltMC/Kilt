// TRACKED HASH: 7ec032f8735b23aa563858eb4a3555caa9c5d7ff
package xyz.bluspring.kilt.injects.world.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.server.timings.TimeTracker;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;

@Mixin(value = Level.class, priority = 1111) // higher priority to mixin to Porting Lib
@Extends(AttachmentHolder.class)
public abstract class LevelInject implements LevelAccessor, ILevelExtension, LevelInjection {
    public boolean restoringBlockSnapshots = false;
    public boolean captureBlockSnapshots = false;

    @Unique private final ArrayList<BlockEntity> freshBlockEntities = new ArrayList<>();
    @Unique private final ArrayList<BlockEntity> pendingFreshBlockEntities = new ArrayList<>();

    @Shadow @Final public boolean isClientSide;
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow public abstract BlockState getBlockState(BlockPos blockPos);
    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);
    @Shadow @Final private ResourceKey<Level> dimension;

    @Shadow
    private boolean tickingBlockEntities;
    public ArrayList<BlockSnapshot> capturedBlockSnapshots = new ArrayList<>();

    @Override
    public ArrayList<BlockSnapshot> kilt$getCapturedBlockSnapshots() {
        return capturedBlockSnapshots;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$captureSnapshot(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<BlockPos> posRef, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        posRef.set(pos.immutable());

        if (this.captureBlockSnapshots && !this.isClientSide) {
            blockSnapshot.set(BlockSnapshot.create(this.dimension, (Level) (Object) this, posRef.get()));
            this.capturedBlockSnapshots.add(blockSnapshot.get());
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "RETURN", ordinal = 2))
    private void kilt$removeCapturedSnapshot(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        if (blockSnapshot.get() != null) {
            this.capturedBlockSnapshots.remove(blockSnapshot.get());
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;updatePOIOnBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", shift = At.Shift.AFTER))
    private void kilt$callOtherBlockStateChange(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockState newState) {
        state.onBlockStateChange((Level) (Object) this, pos, newState);
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void kilt$cancelIfCapturing(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        if (blockSnapshot.get() != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateNeighborsAt", at = @At("TAIL"))
    public void kilt$notifyNeighbours(BlockPos pos, Block sourceBlock, Orientation orientation, CallbackInfo ci) {
        // why is "isCanceled()" added at the end?
        EventHooks.onNeighborNotify((Level) (Object) this, pos, this.getBlockState(pos), EnumSet.allOf(Direction.class), false).isCanceled();
    }

    @Override
    public void addFreshBlockEntities(Collection<BlockEntity> list) {
        if (this.tickingBlockEntities) {
            this.pendingFreshBlockEntities.addAll(list);
        } else {
            this.freshBlockEntities.addAll(list);
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void kilt$addAllPendingFreshBlockEntities(CallbackInfo ci) {
        if (!this.pendingFreshBlockEntities.isEmpty()) {
            this.freshBlockEntities.addAll(this.pendingFreshBlockEntities);
            this.pendingFreshBlockEntities.clear();
        }
    }

    @Definition(id = "tickingBlockEntities", field = "Lnet/minecraft/world/level/Level;tickingBlockEntities:Z")
    @Expression("this.tickingBlockEntities = true")
    @Inject(method = "tickBlockEntities", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$loadAllFreshBlockEntities(CallbackInfo ci) {
        if (!this.freshBlockEntities.isEmpty()) {
            this.freshBlockEntities.forEach(blockEntity -> {
                if (!blockEntity.isRemoved() && blockEntity.hasLevel()) {
                    blockEntity.onLoad();
                }
            });

            this.freshBlockEntities.clear();
        }
    }

    // Kilt: Removing erroring entities should be given to another mod like Neruina, we don't have a reason to implement that.

    @WrapMethod(method = "guardEntityTick")
    private <T extends Entity> void kilt$handleTracking(Consumer<T> consumerEntity, T entity, Operation<Void> original) {
        try {
            TimeTracker.ENTITY_UPDATE.trackStart(entity);
            original.call(consumerEntity, entity);
        } finally {
            TimeTracker.ENTITY_UPDATE.trackEnd(entity);
        }
    }

    @Definition(id = "getGameRules", method = "Lnet/minecraft/world/level/Level;getGameRules()Lnet/minecraft/world/level/GameRules;")
    @Definition(id = "getBoolean", method = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
    @Definition(id = "RULE_MOBGRIEFING", field = "Lnet/minecraft/world/level/GameRules;RULE_MOBGRIEFING:Lnet/minecraft/world/level/GameRules$Key;")
    @Expression("this.getGameRules().getBoolean(RULE_MOBGRIEFING)")
    @ModifyExpressionValue(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;ZLnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/Explosion;", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanEntityGrief(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || EventHooks.canEntityGrief((Level) (Object) this, entity);
    }

    @Inject(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;ZLnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/Explosion;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;explode()V"), cancellable = true)
    private void kilt$checkExplosionStartEvent(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, boolean spawnParticles, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        if (EventHooks.onExplosionStart((Level) (Object) this, explosion))
            cir.setReturnValue(explosion);
    }

    @Inject(method = "removeBlockEntity", at = @At("TAIL"))
    public void kilt$updateNeighbourOutputSignalsForRemoval(BlockPos pos, CallbackInfo ci) {
        this.updateNeighbourForOutputSignal(pos, this.getBlockState(pos).getBlock());
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("TAIL"))
    private void kilt$addPartEntitiesToList(Entity entity, AABB area, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir, @Local List<Entity> list) {
        for (PartEntity<?> partEntity : this.kilt$getPartEntities()) {
            if (partEntity != entity && partEntity.getBoundingBox().intersects(area) && predicate.test(partEntity)) {
                list.add(partEntity);
            }
        }
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("TAIL"))
    private <T extends Entity> void kilt$addPartEntitiesToList(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate, List<? super T> output, int maxResults, CallbackInfo ci) {
        for (PartEntity<?> partEntity : this.kilt$getPartEntities()) {
            var castEntity = entityTypeTest.tryCast(partEntity);
            if (castEntity != null && partEntity.getBoundingBox().intersects(bounds) && predicate.test(castEntity)) {
                // TODO: Kilt: doesn't this technically overflow the maxResults?
                output.add(castEntity);
                if (output.size() >= maxResults)
                    break;
            }
        }
    }

    @ModifyExpressionValue(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction$Plane;iterator()Ljava/util/Iterator;"))
    private Iterator<Direction> kilt$addMoreNeighbourDirections(Iterator<Direction> original) {
        return Arrays.stream(Direction.values()).iterator();
    }

    @WrapOperation(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    public boolean kilt$checkForNeighbourChange(BlockState instance, Block block, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) BlockPos blockPos, @Local(ordinal = 1) BlockPos directionPos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "onNeighborChange", BlockState.class, LevelReader.class, BlockPos.class, BlockPos.class)) {
            instance.onNeighborChange((Level) (Object) this, directionPos, blockPos);
            // Don't trigger the Vanilla neighbour change.
            return false;
        }

        return original.call(instance, block);
    }

    @WrapOperation(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 1))
    public boolean kilt$getWeakChange(BlockState instance, Block block, Operation<Boolean> original, @Local(ordinal = 1) BlockPos directionPos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "getWeakChanges", BlockState.class, LevelReader.class, BlockPos.class)) {
            return instance.getWeakChanges((Level) (Object) this, directionPos);
        }

        return original.call(instance, block);
    }

    private double maxEntityRadius = 2.0D;

    @Override
    public double getMaxEntityRadius() {
        return maxEntityRadius;
    }

    @Override
    public double increaseMaxEntityRadius(double value) {
        if (value > maxEntityRadius)
            maxEntityRadius = value;

        return maxEntityRadius;
    }

    @ApiStatus.Internal @Override public abstract void setDayTimeFraction(float dayTimeFraction);
    @ApiStatus.Internal @Override public abstract float getDayTimeFraction();
    @Override public abstract float getDayTimePerTick();
    @Override public abstract void setDayTimePerTick(float dayTimePerTick);

    @Override
    public long advanceDaytime() {
        if (this.getDayTimePerTick() < 0) {
            return 1L;
        }

        float dayTimeStep = this.getDayTimeFraction() + this.getDayTimePerTick();
        long result = (long) dayTimeStep;
        this.setDayTimeFraction(dayTimeStep - result);

        return result;
    }

    // Kilt
    @Override
    public void kilt$setCapturingBlockSnapshots(boolean value) {
        this.captureBlockSnapshots = value;
    }

    @Override
    public void kilt$setRestoringBlockSnapshots(boolean value) {
        this.restoringBlockSnapshots = value;
    }

    @Override
    public boolean kilt$getCapturingBlockSnapshots() {
        return this.captureBlockSnapshots;
    }

    @Override
    public boolean kilt$getRestoringBlockSnapshots() {
        return this.restoringBlockSnapshots;
    }
}
