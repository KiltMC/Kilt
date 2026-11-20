package xyz.bluspring.kilt.injects.client.multiplayer;

import java.util.Collection;
import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.neoforged.neoforge.client.ColorResolverManager;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelDataManager;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.multiplayer.ClientLevelInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.HitResult;

@Mixin(ClientLevel.class)
public abstract class ClientLevelInject extends Level implements ClientLevelInjection {
    @Shadow @Final private BlockStatePredictionHandler blockStatePredictionHandler;

    @Shadow
    public abstract void addBreakingBlockEffect(BlockPos pos, Direction direction);

    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique private final List<PartEntity<?>> kilt$partEntities = Lists.newArrayList();
    @Unique private final ModelDataManager modelDataManager = new ModelDataManager(this);

    protected ClientLevelInject(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(method = "lambda$new$0", at = @At("TAIL"))
    private void kilt$initTintCaches(Object2ObjectArrayMap<ColorResolver, BlockTintCache> cache, CallbackInfo ci) {
        ColorResolverManager.registerBlockTintCaches((ClientLevel) (Object) this, cache);
    }

    @Definition(id = "oldState", local = @Local(type = BlockState.class, name = "oldState"))
    @Definition(id = "getBlockState", method = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Definition(id = "pos", local = @Local(type = BlockPos.class, name = "pos", argsOnly = true))
    @Expression("oldState = this.getBlockState(pos)")
    @Inject(method = "setBlock", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$storeBlockSnapshot(BlockPos pos, BlockState blockState, int updateFlags, int updateLimit, CallbackInfoReturnable<Boolean> cir, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {
        snapshotRef.set(BlockSnapshot.create(this.dimension(), this, pos, updateFlags));
    }

    @Inject(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/prediction/BlockStatePredictionHandler;retainKnownServerState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/player/LocalPlayer;)V", shift = At.Shift.AFTER))
    private void kilt$retainSnapshot(BlockPos pos, BlockState blockState, int updateFlags, int updateLimit, CallbackInfoReturnable<Boolean> cir, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {
        this.blockStatePredictionHandler.retainSnapshot(pos, snapshotRef.get());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$loadLevelEvent(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(this));
    }

    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void kilt$tryHandleTickEvent(Entity instance, Operation<Void> original) {
        if (!EventHooks.fireEntityTickPre(instance).isCanceled()) {
            original.call(instance);
            EventHooks.fireEntityTickPost(instance);
        }
    }

    @Inject(at = @At("TAIL"), method = "addEntity")
    public void kilt$addEntityToWorld(Entity entity, CallbackInfo ci) {
        entity.onAddedToLevel();
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$callPlayLevelSoundAtPositionEvent(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true) LocalRef<SoundSource> source, @Local(argsOnly = true, name = "volume") LocalFloatRef volume, @Local(argsOnly = true, name = "pitch") LocalFloatRef pitch, @Local(argsOnly = true, name = "x") double x, @Local(argsOnly = true, name = "y") double y, @Local(argsOnly = true, name = "z") double z) {
        var event = EventHooks.onPlaySoundAtPosition(this, x, y, z, sound.get(), source.get(), volume.get(), pitch.get());

        if (event.isCanceled() || event.getSound() == null) {
            ci.cancel();
            return;
        }

        sound.set(event.getSound());
        source.set(event.getSource());
        volume.set(event.getNewVolume());
        pitch.set(event.getNewPitch());
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$callPlayLevelSoundAtEntityEvent(CallbackInfo ci, @Local(argsOnly = true, name = "sound") LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true, name = "source") LocalRef<SoundSource> source, @Local(argsOnly = true, name = "volume") LocalFloatRef volume, @Local(argsOnly = true, name = "pitch") LocalFloatRef pitch, @Local(argsOnly = true, name = "sourceEntity") Entity entity) {
        var event = EventHooks.onPlaySoundAtEntity(entity, sound.get(), source.get(), volume.get(), pitch.get());

        if (event.isCanceled() || event.getSound() == null) {
            ci.cancel();
            return;
        }

        sound.set(event.getSound());
        source.set(event.getSource());
        volume.set(event.getNewVolume());
        pitch.set(event.getNewPitch());
    }

    @Unique private HitResult kilt$hitResult;

    @Override
    public void addBreakingBlockEffect(BlockPos pos, Direction direction, @Nullable HitResult hitResult) {
        try {
            this.kilt$hitResult = hitResult;
            this.addBreakingBlockEffect(pos, direction);
        } finally {
            this.kilt$hitResult = null;
        }
    }

    @WrapOperation(method = "addBreakingBlockEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;shouldSpawnTerrainParticles()Z"))
    private boolean kilt$checkShouldAddHitEffects(BlockState instance, Operation<Boolean> original) {
        var ext = IClientBlockExtensions.of(instance);
        if (ext != IClientBlockExtensions.DEFAULT) {
            return !ext.addHitEffects(instance, this, this.kilt$hitResult, this.minecraft.particleEngine);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "addBreakingBlockEffect", at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/particle/TerrainParticle;"))
    private TerrainParticle kilt$handleUpdateSprite(ClientLevel level, double x, double y, double z, double xa, double ya, double za, BlockState blockState, BlockPos pos, Operation<TerrainParticle> original) {
        return original.call(level, x, y, z, xa, ya, za, blockState, pos)
            .updateSprite(blockState, pos);
    }

    @Mixin(ClientLevel.ClientLevelData.class)
    public abstract static class ClientLevelDataInject {
        @Shadow private Difficulty difficulty;

        @Inject(method = "setDifficulty", at = @At("HEAD"))
        private void kilt$callForgeDifficultyChangeEvent(Difficulty difficulty, CallbackInfo ci) {
            CommonHooks.onDifficultyChange(difficulty, this.difficulty);
        }
    }

    @Mixin(targets = "net/minecraft/client/multiplayer/ClientLevel$EntityCallbacks")
    public abstract static class EntityCallbacksInject {
        @Shadow @Final ClientLevel this$0;

        @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$addForgeMultipartEntitiesToLevel(Entity entity, CallbackInfo ci) {
            if (entity.isMultipartEntity() && entity.getParts() != null) {
                for (PartEntity<?> part : entity.getParts()) {
                    this$0.kilt$getPartEntities().add(part);
                }
            }
        }

        @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$removeForgeMultipartEntitiesFromLevel(Entity entity, CallbackInfo ci) {
            entity.onRemovedFromLevel();
            NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, this$0));

            if (entity.isMultipartEntity() && entity.getParts() != null) {
                for (PartEntity<?> part : entity.getParts()) {
                    this$0.kilt$getPartEntities().remove(part);
                }
            }
        }
    }

    @Override
    public Collection<PartEntity<?>> kilt$getPartEntities() {
        return this.kilt$partEntities;
    }

    @Override
    public ModelDataManager getModelDataManager() {
        return modelDataManager;
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return modelDataManager.getAt(pos);
    }
}
