package xyz.bluspring.kilt.compat.create.registrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;

@FunctionalInterface
public interface FluidTypeFactoryToken {
    FluidType create(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture);
}
