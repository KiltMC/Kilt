package xyz.bluspring.kilt.injects.world.level.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockInject extends Block {
    public CauldronBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"), cancellable = true)
    private void kilt$checkHandleCauldronDrip(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (fluid.getFluidType().handleCauldronDrip(fluid, level, pos))
            ci.cancel();
    }
}
