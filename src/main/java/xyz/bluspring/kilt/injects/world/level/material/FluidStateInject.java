// TRACKED HASH: da4c7d69266cd226e6baab431327d61c6d9516d3
package xyz.bluspring.kilt.injects.world.level.material;

import net.neoforged.neoforge.common.extensions.IFluidStateExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.workarounds.FluidWorkaround;

import net.minecraft.world.level.material.FluidState;

@Implements(@Interface(iface = FluidWorkaround.class, prefix = "kilt$i$"))
@Mixin(FluidState.class)
public abstract class FluidStateInject implements IFluidStateExtension {
    @Intrinsic
    public FluidType kilt$i$getFluidType() {
        return this.getFluidType();
    }
}
