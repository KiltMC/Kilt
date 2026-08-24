package xyz.bluspring.kilt.injects.world.level.chunk;

import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.attachment.AttachmentInternals;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.common.world.LevelChunkAuxiliaryLightManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.CommonLevelWorkaround;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.storage.ValueInput;

@Implements(@Interface(iface = CommonLevelWorkaround.class, prefix = "kilt$i$", remap = Interface.Remap.NONE))
@Mixin(LevelChunk.class)
public abstract class LevelChunkInject extends ChunkAccess implements IAttachmentHolder {
    @Shadow @Final private Level level;

    public LevelChunkInject(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory containerFactory, long inhabitedTime, LevelChunkSection @org.jspecify.annotations.Nullable [] sections, @org.jspecify.annotations.Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, containerFactory, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setAllReferences(Ljava/util/Map;)V", shift = At.Shift.AFTER))
    private void kilt$copyChunkAttachments(ServerLevel level, ProtoChunk chunk, LevelChunk.PostLoadProcessor postLoad, CallbackInfo ci) {
        AttachmentInternals.copyChunkAttachmentsOnPromotion(level.registryAccess(), chunk.getAttachmentHolder(), this.getAttachmentHolder());
    }

    @Definition(id = "level", field = "Lnet/minecraft/world/level/chunk/LevelChunk;level:Lnet/minecraft/world/level/Level;")
    @Definition(id = "isClientSide", method = "Lnet/minecraft/world/level/Level;isClientSide()Z")
    @Expression("this.level.isClientSide() == 0")
    @ModifyExpressionValue(method = "setBlockState", at = @At("MIXINEXTRAS:EXPRESSION"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V")))
    private boolean kilt$checkShouldLevelCaptureSnapshots(boolean original) {
        return original && !this.level.kilt$getCapturingBlockSnapshots();
    }

    @Definition(id = "blockEntities", field = "Lnet/minecraft/world/level/chunk/LevelChunk;blockEntities:Ljava/util/Map;")
    @Definition(id = "get", method = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    @Expression("this.blockEntities.get(?)")
    @WrapOperation(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("MIXINEXTRAS:EXPRESSION"))
    private <K, V> V kilt$removeBlockEntityIfRemoved(Map<K, V> instance, K o, Operation<V> original) {
        var value = original.call(instance, o);

        if (value != null && value instanceof BlockEntity blockEntity && blockEntity.isRemoved()) {
            instance.remove(o);
            return null;
        }

        return value;
    }

    @WrapOperation(method = "addAndRegisterBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private <T extends BlockEntity> void kilt$callAddFreshBlockEntities(LevelChunk instance, T blockEntity, Operation<Void> original) {
        original.call(instance, blockEntity);
        instance.getLevel().addFreshBlockEntities(List.of(blockEntity));
    }

    // Kilt: not doing block entity guarding, that's another mod's job :D

    @Inject(method = {"setBlockEntity", "removeBlockEntity"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V", shift = At.Shift.AFTER))
    private void kilt$removeLightFromAuxLightManager(CallbackInfo ci, @Local BlockPos pos) {
        this.auxLightManager.removeLightAt(pos);
    }

    @WrapOperation(method = "lambda$replaceWithPacketData$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void kilt$tryLoadWithUpdateTag(BlockEntity instance, ValueInput input, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IBlockEntityExtension.class, "handleUpdateTag", ValueInput.class)) {
            instance.handleUpdateTag(input);
        } else {
            original.call(instance, input);
        }
    }

    @Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
    private void kilt$unloadAllBlockEntities(CallbackInfo ci) {
        this.blockEntities.values().forEach(BlockEntity::onChunkUnloaded);
    }

    @Inject(method = "registerAllBlockEntitiesAfterLevelLoad", at = @At("HEAD"))
    private void kilt$addAllFreshBlockEntities(CallbackInfo ci) {
        this.level.addFreshBlockEntities(this.blockEntities.values());
    }

    @Unique private final LevelChunkAuxiliaryLightManager auxLightManager = new LevelChunkAuxiliaryLightManager((LevelChunk) (Object) this);

    @Override
    public @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return this.auxLightManager;
    }

    @Override
    public void syncData(AttachmentType<?> type) {
        AttachmentSync.syncChunkUpdate((LevelChunk) (Object) this, this.getAttachmentHolder(), type);
    }

    // Kilt: do we need to do tracking?

    @Intrinsic
    public Level kilt$i$getLevel() { // that's bullshit
        return this.getLevel();
    }
}
