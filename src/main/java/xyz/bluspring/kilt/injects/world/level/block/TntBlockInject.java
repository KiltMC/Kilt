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
    private static void explode(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    public TntBlockInject(Properties properties) {
        super(properties);
    }

    public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        explode(world, pos, igniter);
    }

    @WrapOperation(method = {"onPlace", "neighborChanged", "playerWillDestroy"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), TntBlock.class, "onCaughtFire", BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            this.onCaughtFire(state, level, pos, null, null);
        } else {
            original.call(level, pos);
        }
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, LivingEntity entity, Operation<Void> original, @Local(argsOnly = true) BlockHitResult result, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), TntBlock.class, "onCaughtFire", BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            this.onCaughtFire(state, level, pos, result.getDirection(), entity);
        } else {
            original.call(level, pos, entity);
        }
    }

    @WrapOperation(method = "onProjectileHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void kilt$callExplodeOrCaughtFire2(Level level, BlockPos pos, LivingEntity entity, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), TntBlock.class, "onCaughtFire", BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class)) {
            this.onCaughtFire(state, level, pos, null, entity);
        } else {
            original.call(level, pos, entity);
        }
    }
}
