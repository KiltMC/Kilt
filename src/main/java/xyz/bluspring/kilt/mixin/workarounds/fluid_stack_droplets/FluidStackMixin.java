package xyz.bluspring.kilt.mixin.workarounds.fluid_stack_droplets;

import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.workarounds.ForgeFluidStackWorkaround;

@Implements(@Interface(iface = ForgeFluidStackWorkaround.class, prefix = "kilt$i$"))
@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Shadow
    public abstract int forge$getAmount();

    @Intrinsic(displace = true)
    public int kilt$i$getAmount() {
        return this.forge$getAmount();
    }
}
