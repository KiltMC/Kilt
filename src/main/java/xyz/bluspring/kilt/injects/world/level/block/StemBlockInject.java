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
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.block.CropBlockInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(StemBlock.class)
public abstract class StemBlockInject extends BushBlock {
    protected StemBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "mayPlaceOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean kilt$checkCanSustainPlant2(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.getBlock() instanceof FarmBlock;
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void kilt$avoidLoadingUnloadedChunks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("originalState") LocalRef<BlockState> originalState) {
        originalState.set(state);

        if (!level.isAreaLoaded(pos, 1))
            ci.cancel();
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CropBlock;getGrowthSpeed(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float kilt$storeGrowthState(Block block, BlockGetter level, BlockPos pos, Operation<Float> original, @Local(argsOnly = true) BlockState state) {
        try {
            CropBlockInjection.kilt$currentState.set(state);
            return original.call(block, level, pos);
        } finally {
            CropBlockInjection.kilt$currentState.remove();
        }
    }

    @Definition(id = "random", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("random.nextInt((int)(25.0 / f) + 1) == 0")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callForgePreGrow(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return CommonHooks.canCropGrow(level, pos, state, original);
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkIsEmpty(BlockState instance, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos) {
        return original.call(instance) || level.isEmptyBlock(pos);
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean kilt$checkCanSustainPlant(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos) {
        return original.call(instance, block) || instance.getBlock() instanceof FarmBlock;
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", shift = At.Shift.AFTER, ordinal = 0))
    private void kilt$markCanPostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("canPostGrow") LocalBooleanRef canPostGrow) {
        canPostGrow.set(true);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void kilt$callForgePostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("canPostGrow") LocalBooleanRef canPostGrow, @Share("originalState") LocalRef<BlockState> originalState) {
        if (canPostGrow.get()) {
            CommonHooks.fireCropGrowPost(level, pos, originalState.get());
        }
    }
}
