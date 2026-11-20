package xyz.bluspring.kilt.injects.world.level.portal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

@Mixin(PortalShape.class)
public abstract class PortalShapeInject {
    @ModifyReturnValue(method = "lambda$static$0", at = @At("RETURN"))
    private static boolean kilt$checkIsPortalFrame(boolean original, BlockState state, BlockGetter level, BlockPos pos) {
        return original || state.isPortalFrame(level, pos);
    }
}
