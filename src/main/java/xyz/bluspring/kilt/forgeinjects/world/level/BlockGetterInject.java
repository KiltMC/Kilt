// TRACKED HASH: 002bbb6e1c497349b378b18204f13731d00bfb13
package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(BlockGetter.class)
public interface BlockGetterInject extends IForgeBlockGetter {
    @WrapOperation(method = "getLightEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int kilt$tryUseForgeEmission(BlockState instance, Operation<Integer> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getLightEmission", BlockState.class, BlockGetter.class, BlockPos.class)) {
            return instance.getLightEmission((BlockGetter) this, pos);
        }

        return original.call(instance);
    }
}