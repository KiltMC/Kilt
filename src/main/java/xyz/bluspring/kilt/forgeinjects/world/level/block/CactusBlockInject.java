// TRACKED HASH: 6dda8abff7a6be7812b1b340b29b5929c097c46b
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
public abstract class CactusBlockInject implements IPlantable {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kilt$preventLoadUnloadedChunk(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.isAreaLoaded(pos, 1))
            ci.cancel();
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0), cancellable = true)
    private void kilt$callCropGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (!ForgeHooks.onCropsGrowPre(level, pos.above(), state, true)) {
            ci.cancel();
            return;
        }

        shouldRunPostEvent.set(true);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void kilt$callCropGrowPostEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (shouldRunPostEvent.get()) {
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkCanSustainPlant(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        return original.call(instance, tagKey) || instance.canSustainPlant(level, pos, Direction.UP, this);
    }

    @Override
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.DESERT;
    }
}