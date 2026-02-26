package xyz.bluspring.kilt.workarounds;

import net.neoforged.neoforge.fluids.FluidType;
import xyz.bluspring.kilt.util.KiltHelper;

public interface FluidWorkaround {
    default FluidType getFluidType() {
        throw KiltHelper.createMixinException(FluidWorkaround.class, "getFluidType");
    }
}
