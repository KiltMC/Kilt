// TRACKED HASH: 366d1f3fc3927ab11616be112b636f33f5fe6585
package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeLevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;
import xyz.bluspring.kilt.injections.world.level.chunk.LevelChunkInjection;

import java.util.List;
import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class LevelChunkInject extends ChunkAccess implements ChunkAccessInjection, IForgeLevelChunk, LevelChunkInjection {
    @Unique private final CapabilityProvider.AsField<LevelChunk> capProvider = new CapabilityProvider.AsField<>(LevelChunk.class, (LevelChunk) (Object) this);

    public LevelChunkInject(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("TAIL"))
    private void kilt$initCaps(Level level, ChunkPos pos, UpgradeData data, LevelChunkTicks blockTicks, LevelChunkTicks fluidTicks, long inhabitedTime, LevelChunkSection[] sections, LevelChunk.PostLoadProcessor postLoad, BlendingData blendingData, CallbackInfo ci) {
        this.capProvider.initInternal();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return capProvider.getCapability(cap);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return capProvider.getCapability(cap, side);
    }

    @Override
    public boolean areCapsCompatible(CapabilityProvider<LevelChunk> other) {
        return capProvider.areCapsCompatible(other);
    }

    @Override
    public boolean areCapsCompatible(@Nullable CapabilityDispatcher other) {
        return capProvider.areCapsCompatible(other);
    }

    @Override
    public void invalidateCaps() {
        capProvider.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        capProvider.reviveCaps();
    }

    @Shadow public abstract Level getLevel();

    @Shadow @Final private Level level;

    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return this.getLevel();
    }

    @Inject(method = "addAndRegisterBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", shift = At.Shift.AFTER))
    public void kilt$loadBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        blockEntity.onLoad();
    }

    @Redirect(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    public void kilt$handleBlockEntityUpdate(BlockEntity instance, CompoundTag tag) {
        instance.handleUpdateTag(tag);
    }

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean kilt$verifyBlockEntityToo(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) BlockState state) {
        return original.call(instance, block) || state.hasBlockEntity();
    }

    @WrapOperation(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    private boolean kilt$validateNotCapturingSnapshots(Level instance, Operation<Boolean> original) {
        return original.call(instance); // || ((LevelInjection) instance).isCaptureBlockSnapshots();
    }

    @WrapOperation(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V kilt$returnNullIfRemoved(Map<K, V> instance, Object o, Operation<V> original, @Local(argsOnly = true) BlockPos pos) {
        BlockEntity blockEntity = (BlockEntity) original.call(instance, o);

        if (blockEntity != null && blockEntity.isRemoved()) {
            this.blockEntities.remove(pos);
            return null;
        }

        return (V) blockEntity;
    }

    @Redirect(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;isRemoved()Z"))
    private boolean kilt$disableBlockEntityRemove(BlockEntity instance) {
        return false;
    }

    @Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
    private void kilt$unloadBlockEntities(CallbackInfo ci) {
        this.blockEntities.values().forEach(BlockEntity::onChunkUnloaded);
    }

    @Inject(method = "registerAllBlockEntitiesAfterLevelLoad", at = @At("HEAD"))
    private void kilt$addBlockEntitiesToLevel(CallbackInfo ci) {
        this.level.addFreshBlockEntities(this.blockEntities.values());
    }

    @Inject(method = "addAndRegisterBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void kilt$addBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        this.level.addFreshBlockEntities(List.of(blockEntity));
    }

    @Override
    public CompoundTag writeCapsToNBT() {
        return capProvider.serializeInternal();
    }

    @Override
    public void readCapsFromNBT(CompoundTag tag) {
        capProvider.deserializeInternal(tag);
    }
}