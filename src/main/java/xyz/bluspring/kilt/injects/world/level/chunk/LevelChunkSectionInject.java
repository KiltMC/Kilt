package xyz.bluspring.kilt.injects.world.level.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionInject {
    @WrapOperation(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$tryFixMC232360(BlockState instance, Operation<Boolean> original) {
        return original.call(instance) || instance.isEmpty();
    }

    @Mixin(targets = "net.minecraft.world.level.chunk.LevelChunkSection$1BlockCounter")
    public abstract static class BlockCounterInject {
        @WrapOperation(method = "accept(Lnet/minecraft/world/level/block/state/BlockState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
        private boolean kilt$tryFixMC232360(BlockState instance, Operation<Boolean> original) {
            return original.call(instance) || instance.isEmpty();
        }
    }
}
