package xyz.bluspring.kilt.injects.world.level.block.piston;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverInject {
    @Shadow @Final private Level level;

    @ModifyReturnValue(method = "isSticky", at = @At("RETURN"))
    private static boolean kilt$checkIsSticky(boolean original, @Local(argsOnly = true) BlockState state) {
        return original || state.isStickyBlock();
    }

    @ModifyReturnValue(method = "canStickToEachOther", at = @At(value = "RETURN", ordinal = 2))
    private static boolean kilt$checkCanStickEachOther(boolean original, BlockState state1, BlockState state2) {
        if (!state1.canStickTo(state2) || !state2.canStickTo(state1))
            return false;

        return original;
    }
}
