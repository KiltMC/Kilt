package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.extensions.IForgeLevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(LevelChunk.class)
public abstract class LevelChunkInject extends ChunkAccessInject implements ChunkAccessInjection, IForgeLevelChunk {
    @Shadow public abstract Level getLevel();

    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return this.getLevel();
    }

    @Inject(method = "addAndRegisterBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", shift = At.Shift.AFTER))
    public void kilt$loadBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        blockEntity.onLoad();
    }

    @WrapOperation(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    public void kilt$handleBlockEntityUpdate(BlockEntity instance, CompoundTag tag, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BlockEntity.class, "handleUpdateTag", CompoundTag.class)) {
            instance.handleUpdateTag(tag);
            return;
        }

        original.call(instance, tag);
    }

    @Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
    private void kilt$unloadBlockEntityChunks(CallbackInfo ci) {
        this.blockEntities.values().forEach(BlockEntity::onChunkUnloaded);
    }
}
