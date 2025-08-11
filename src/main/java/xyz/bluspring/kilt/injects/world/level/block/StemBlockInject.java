// TRACKED HASH: 82ada9f43bdf1d0e2344c78ce5690546e351840c
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IPlantable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StemBlock.class)
public abstract class StemBlockInject {
    @Shadow @Final private StemGrownBlock fruit;

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void kilt$avoidLoadingUnloadedChunks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("originalState") LocalRef<BlockState> originalState) {
        originalState.set(state);

        if (!level.isAreaLoaded(pos, 1))
            ci.cancel();
    }

    @Definition(id = "random", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("random.nextInt((int)(25.0 / f) + 1) == 0")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callForgePreGrow(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return CommonHooks.onCropsGrowPre(level, pos, state, original);
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkIsEmpty(BlockState instance, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos) {
        return original.call(instance) || level.isEmptyBlock(pos);
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean kilt$checkCanSustainPlant(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos) {
        return original.call(instance, block) || instance.canSustainPlant(level, pos.below(), Direction.UP, (IPlantable) this.fruit);
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", shift = At.Shift.AFTER, ordinal = 0))
    private void kilt$markCanPostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("canPostGrow") LocalBooleanRef canPostGrow) {
        canPostGrow.set(true);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void kilt$callForgePostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("canPostGrow") LocalBooleanRef canPostGrow, @Share("originalState") LocalRef<BlockState> originalState) {
        if (canPostGrow.get()) {
            CommonHooks.onCropsGrowPost(level, pos, originalState.get());
        }
    }
}