package xyz.bluspring.kilt.forgeinjects.world.level.portal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortalShape.class)
public abstract class PortalShapeInject {
    @ModifyReturnValue(method = "method_30487", at = @At("RETURN"))
    private static boolean kilt$checkIsPortalFrame(boolean original, BlockState state, BlockGetter level, BlockPos pos) {
        return original || state.isPortalFrame(level, pos);
    }
}
