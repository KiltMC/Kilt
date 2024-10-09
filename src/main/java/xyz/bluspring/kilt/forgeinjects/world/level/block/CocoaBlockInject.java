package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CocoaBlock.class)
public abstract class CocoaBlockInject {
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int kilt$storeRandomResult(RandomSource instance, int i, Operation<Integer> original, @Share("originalResult") LocalIntRef originalResult) {
        originalResult.set(original.call(instance, i));
        return 0;
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean kilt$callCropEvents(ServerLevel instance, BlockPos blockPos, BlockState blockState, int i, Operation<Boolean> original, @Share("originalResult") LocalIntRef originalResult, @Local(argsOnly = true) BlockState state) {
        if (ForgeHooks.onCropsGrowPre(instance, blockPos, state, originalResult.get() == 0)) {
            var value = original.call(instance, blockPos, blockState, i);
            ForgeHooks.onCropsGrowPost(instance, blockPos, state);

            return value;
        }

        return false;
    }
}
