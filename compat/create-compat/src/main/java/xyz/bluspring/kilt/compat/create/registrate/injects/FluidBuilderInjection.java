package xyz.bluspring.kilt.compat.create.registrate.injects;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;

public interface FluidBuilderInjection {
    ResourceLocation kilt$getStillTexture();

    ResourceLocation kilt$getFlowingTexture();

    FluidType.Properties kilt$makeTypeProperties();
}
