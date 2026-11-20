// TRACKED HASH: da4c7d69266cd226e6baab431327d61c6d9516d3
package xyz.bluspring.kilt.injects.world.level.material;

import net.neoforged.neoforge.common.extensions.IFluidStateExtension;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.material.FluidState;

@Mixin(FluidState.class)
public abstract class FluidStateInject implements IFluidStateExtension {
}
