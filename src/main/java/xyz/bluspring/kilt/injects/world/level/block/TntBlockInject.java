package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(TntBlock.class)
public abstract class TntBlockInject extends Block {
    @Shadow
    private static boolean prime(Level par1, BlockPos par2, LivingEntity par3) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    public TntBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        return prime(world, pos, igniter);
    }

    @WrapOperation(method = {"onPlace", "neighborChanged", "playerWillDestroy"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), TntBlock.class, "onCaughtFire", boolean.class, BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            return this.onCaughtFire(state, level, pos, null, null);
        } else {
            return original.call(level, pos);
        }
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, LivingEntity entity, Operation<Boolean> original, @Local(argsOnly = true) BlockHitResult result, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), TntBlock.class, "onCaughtFire", boolean.class, BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            return this.onCaughtFire(state, level, pos, result.getDirection(), entity);
        } else {
            return original.call(level, pos, entity);
        }
    }

    @WrapOperation(method = "onProjectileHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean kilt$callExplodeOrCaughtFire2(Level level, BlockPos pos, LivingEntity entity, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), TntBlock.class, "onCaughtFire", boolean.class, BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            return this.onCaughtFire(state, level, pos, null, entity);
        } else {
            return original.call(level, pos, entity);
        }
    }
}
