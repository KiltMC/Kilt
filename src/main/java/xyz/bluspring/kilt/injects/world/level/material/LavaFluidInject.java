package xyz.bluspring.kilt.injects.world.level.material;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LavaFluid.class)
public abstract class LavaFluidInject {
    @Unique
    private final ThreadLocal<Direction> kilt$direction = new ThreadLocal<>();

    @WrapOperation(method = "spreadTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$tryFireFluidPlaceBlockEvent(Block instance, Operation<BlockState> original, @Local(argsOnly = true) LevelAccessor level, @Local(argsOnly = true) BlockPos pos) {
        return EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, original.call(instance));
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseFireBlock;getState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$tryFireFluidPlaceBlockEvent(BlockGetter reader, BlockPos pos, Operation<BlockState> original, @Local(argsOnly = true) BlockPos fluidPos) {
        return EventHooks.fireFluidPlaceBlockEvent((LevelAccessor) reader, pos, fluidPos, original.call(reader, pos));
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/LavaFluid;isFlammable(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$setFlammableDirection(LavaFluid instance, LevelReader level, BlockPos pos, Operation<Boolean> original) {
        this.kilt$direction.set(Direction.UP);
        var result = original.call(instance, level, pos);
        this.kilt$direction.remove();
        return result;
    }

    @WrapOperation(method = "hasFlammableNeighbours", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/LavaFluid;isFlammable(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$setFlammableDirection(LavaFluid instance, LevelReader level, BlockPos pos, Operation<Boolean> original, @Local Direction direction) {
        this.kilt$direction.set(direction);
        var result = original.call(instance, level, pos);
        this.kilt$direction.remove();
        return result;
    }

    // Kilt: implemented *specifically* for better compatibility
    @WrapOperation(method = "isFlammable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;ignitedByLava()Z"))
    private boolean kilt$checkIsFlammableByDirection(BlockState instance, Operation<Boolean> original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        return original.call(instance) || instance.isFlammable(level, pos, kilt$direction.get());
    }

    private boolean isFlammable(LevelReader level, BlockPos pos, Direction face) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && !level.hasChunkAt(pos) ? false : level.getBlockState(pos).isFlammable(level, pos, face);
    }
}
