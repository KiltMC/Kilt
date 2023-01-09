package xyz.bluspring.kilt.forgeinjects.world.level.material;

import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeFluidState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidState.class)
public abstract class FluidStateInject implements IForgeFluidState {
}
