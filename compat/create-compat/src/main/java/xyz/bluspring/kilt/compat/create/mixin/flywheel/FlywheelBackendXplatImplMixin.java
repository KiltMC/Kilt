package xyz.bluspring.kilt.compat.create.mixin.flywheel;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@IfModLoaded("flywheel")
@Pseudo
@Mixin(targets = "dev.engine_room.flywheel.backend.FlwBackendXplatImpl")
public abstract class FlywheelBackendXplatImplMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void kilt$flywheel$tryUseNeoLightEmission(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(state.getBlock().getClass(), IBlockExtension.class, "getLightEmission", int.class, BlockState.class, BlockGetter.class, BlockPos.class)) {
            cir.setReturnValue(state.getLightEmission(level, pos));
        }
    }
}
