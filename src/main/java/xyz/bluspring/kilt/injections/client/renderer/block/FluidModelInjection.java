package xyz.bluspring.kilt.injections.client.renderer.block;

import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.sprite.Material;

public interface FluidModelInjection {
    default @Nullable FluidTintSource fluidTintSource() {
        throw KiltHelper.createMixinException(FluidModelInjection.class, "fluidTintSource");
    }

    default @Nullable CustomFluidRenderer customRenderer() {
        throw KiltHelper.createMixinException(FluidModelInjection.class, "customRenderer");
    }

    static FluidModel create(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.@Nullable Baked overlayMaterial, @Nullable FluidTintSource fluidTintSource) {
        return create(layer, stillMaterial, flowingMaterial, overlayMaterial, fluidTintSource, null);
    }

    static FluidModel create(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.@Nullable Baked overlayMaterial, @Nullable FluidTintSource fluidTintSource, @Nullable CustomFluidRenderer customRenderer) {
        var model = new FluidModel(layer, stillMaterial, flowingMaterial, overlayMaterial, fluidTintSource);
        ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.put(model, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, customRenderer));
        return model;
    }

    interface UnbakedInjection {
        default @Nullable FluidTintSource fluidTintSource() {
            throw KiltHelper.createMixinException(UnbakedInjection.class, "fluidTintSource");
        }

        default @Nullable CustomFluidRenderer customRenderer() {
            throw KiltHelper.createMixinException(UnbakedInjection.class, "customRenderer");
        }

        static FluidModel.Unbaked create(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable FluidTintSource fluidTintSource) {
            return create(stillMaterial, flowingMaterial, overlayMaterial, fluidTintSource, null);
        }

        static FluidModel.Unbaked create(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable FluidTintSource fluidTintSource, @Nullable CustomFluidRenderer customRenderer) {
            var model = new FluidModel.Unbaked(stillMaterial, flowingMaterial, overlayMaterial, fluidTintSource);
            ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.put(model, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, customRenderer));
            return model;
        }
    }
}
