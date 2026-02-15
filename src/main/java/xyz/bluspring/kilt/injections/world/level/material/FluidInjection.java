package xyz.bluspring.kilt.injections.world.level.material;

import net.neoforged.neoforge.fluids.FluidType;
import xyz.bluspring.kilt.util.KiltHelper;

public interface FluidInjection {
    default FluidType getFluidType() {
        throw KiltHelper.createMixinException(FluidInjection.class, "getFluidType");
    }
}
