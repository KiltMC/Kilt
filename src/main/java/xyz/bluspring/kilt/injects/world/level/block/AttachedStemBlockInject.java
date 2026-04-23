package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AttachedStemBlock.class)
public abstract class AttachedStemBlockInject extends BushBlock {
    public AttachedStemBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "mayPlaceOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean kilt$checkIsFarmBlock(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.getBlock() instanceof FarmBlock;
    }
}
