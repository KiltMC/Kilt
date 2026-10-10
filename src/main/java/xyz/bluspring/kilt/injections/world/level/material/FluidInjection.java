package xyz.bluspring.kilt.injections.world.level.material;

import java.util.function.Function;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.material.Fluid;

public interface FluidInjection {
    default FluidType getFluidType() {
        throw KiltHelper.createMixinException(FluidInjection.class, "getFluidType");
    }

    default FluidResource computeDefaultResource(Function<Fluid, FluidResource> resourceConstructor) {
        throw KiltHelper.createMixinException(FluidInjection.class, "computeDefaultResource");
    }
}
