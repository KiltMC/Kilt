package xyz.bluspring.mods.flashbacksablecompat.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.mods.flashbacksablecompat.ModSupport;
import xyz.bluspring.mods.flashbacksablecompat.compat.SableSupport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

@Mixin(value = LevelChunk.class, priority = 1050)
public abstract class LevelChunkMixin {
    @Shadow @Final private Level level;

    @Unique
    private void flashback_sable$tryHandleSablePlotBlockChange(BlockPos pos, BlockState state) {
        if (ModSupport.SABLE_LOADED) {
            SableSupport.handleSubLevelPlotBlockChange(level, pos, state);
        }
    }

    @Unique
    private void flashback_sable$tryHandleSableSubLevelBlockChange(BlockPos pos, BlockState oldState, BlockState newState) {
        if (ModSupport.SABLE_LOADED) {
            SableSupport.handleSubLevelBlockChange(level, (LevelChunk) (Object) this, pos, oldState, newState);
        }
    }

    @Unique
    private LevelLightEngine flashback_sable$tryUseSableLightEngine(LevelLightEngine original) {
        if (ModSupport.SABLE_LOADED) {
            return SableSupport.tryUseSableLightEngine(level, (LevelChunk) (Object) this, original);
        }

        return original;
    }

    @TargetHandler(mixin = "com.moulberry.flashback.mixin.playback.MixinLevelChunk", name = "flashback$setBlockStateWithoutUpdates")
    @Inject(method = "@MixinSquared:Handler", at = @At("RETURN"))
    private void tryHandleSableBlockChange(BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<BlockState> cir) {
        this.flashback_sable$tryHandleSablePlotBlockChange(blockPos, blockState);
    }

    @TargetHandler(mixin = "com.moulberry.flashback.mixin.playback.MixinLevelChunk", name = "flashback$setBlockStateWithoutUpdates")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void storeFlashbackSableChange(BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<BlockState> cir, @Share("lightEngine") LocalRef<LevelLightEngine> lightEngineRef, @Local(ordinal = 1) BlockState oldBlockState) {
        flashback_sable$tryHandleSableSubLevelBlockChange(blockPos, oldBlockState, blockState);
    }

    @TargetHandler(mixin = "com.moulberry.flashback.mixin.playback.MixinLevelChunk", name = "flashback$setBlockStateWithoutUpdates")
    @ModifyReceiver(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;updateSectionStatus(Lnet/minecraft/core/BlockPos;Z)V"))
    private LevelLightEngine tryUseSableLightEngine(LevelLightEngine instance, BlockPos blockPos, boolean b) {
        return flashback_sable$tryUseSableLightEngine(instance);
    }

    @TargetHandler(mixin = "com.moulberry.flashback.mixin.playback.MixinLevelChunk", name = "flashback$setBlockStateWithoutUpdates")
    @ModifyReceiver(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;checkBlock(Lnet/minecraft/core/BlockPos;)V"))
    private LevelLightEngine tryUseSableLightEngine(LevelLightEngine instance, BlockPos pos) {
        return flashback_sable$tryUseSableLightEngine(instance);
    }


}
