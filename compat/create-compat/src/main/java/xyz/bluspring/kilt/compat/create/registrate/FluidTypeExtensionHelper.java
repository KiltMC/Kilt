package xyz.bluspring.kilt.compat.create.registrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

/**
 * This class is here to substitute for the anonymous class that would be
 * used in FluidBuilder, but anonymous classes are not allowed in mixins.
 */
public final class FluidTypeExtensionHelper {
    public static IClientFluidTypeExtensions create(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }
        };
    }

    public static FluidType defaultFluidType(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(FluidTypeExtensionHelper.create(stillTexture, flowingTexture));
            }
        };
    }
}
