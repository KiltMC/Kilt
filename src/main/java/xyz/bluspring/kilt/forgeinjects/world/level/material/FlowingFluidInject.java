package xyz.bluspring.kilt.forgeinjects.world.level.material;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidInject extends FluidInject {
    @Shadow protected abstract boolean canConvertToSource(Level level);

    @WrapOperation(method = "getNewLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;isSource()Z"))
    private boolean kilt$checkCanCreateSource(FluidState instance, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 1) BlockState state) {
        return original.call(instance) && ForgeEventFactory.canCreateFluidSource(level, pos, state, instance.canConvertToSource(level, pos));
    }

    @Redirect(method = "getNewLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;canConvertToSource(Lnet/minecraft/world/level/Level;)Z"))
    private boolean kilt$noopConvertToSource(FlowingFluid instance, Level level) {
        return true;
    }

    @Override
    public boolean canConvertToSource(FluidState state, Level level, BlockPos pos) {
        return this.canConvertToSource(level);
    }
}
