package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.compat.create.registrate.FluidBuilderHelper;

@Mixin(FluidBuilderHelper.class) // Only here to force the token replacement to occur!
public abstract class FluidBuilderHelperMixin {
}
