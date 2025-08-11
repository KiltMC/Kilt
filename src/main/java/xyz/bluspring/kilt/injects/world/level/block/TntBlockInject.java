package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fabricators_of_create.porting_lib.block.CaughtFireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(TntBlock.class)
public abstract class TntBlockInject implements CaughtFireBlock {
    // Kilt: Workaround to ensure mod compatibility
    @Unique private final AtomicBoolean kilt$fireCaughtNotReplaced = new AtomicBoolean(false);

    public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        this.kilt$fireCaughtNotReplaced.set(true);
    }

    @WrapOperation(method = {"onPlace", "neighborChanged", "playerWillDestroy"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        this.onCaughtFire(state, level, pos, null, null);

        if (this.kilt$fireCaughtNotReplaced.getAndSet(false)) {
            original.call(level, pos);
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void kilt$callExplodeOrCaughtFire(Level level, BlockPos pos, LivingEntity entity, Operation<Void> original, @Local(argsOnly = true) BlockHitResult result, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        this.onCaughtFire(state, level, pos, result.getDirection(), entity);

        if (this.kilt$fireCaughtNotReplaced.getAndSet(false)) {
            original.call(level, pos, entity);
        }
    }

    @WrapOperation(method = "onProjectileHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void kilt$callExplodeOrCaughtFire2(Level level, BlockPos pos, LivingEntity entity, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) BlockState state) {
        this.onCaughtFire(state, level, pos, null, entity);

        if (this.kilt$fireCaughtNotReplaced.getAndSet(false)) {
            original.call(level, pos, entity);
        }
    }
}
