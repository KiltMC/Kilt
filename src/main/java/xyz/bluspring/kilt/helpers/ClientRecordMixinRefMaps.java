package xyz.bluspring.kilt.helpers;

import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface ClientRecordMixinRefMaps {
    Map<FluidModel, ExtendedFluidModel> EXTENDED_FLUID_MODEL = create();
    Map<FluidModel.Unbaked, ExtendedFluidModel> EXTENDED_UNBAKED_FLUID_MODEL = create();
    record ExtendedFluidModel(@Nullable FluidTintSource fluidTintSource, @Nullable CustomFluidRenderer customRenderer) {}

    Map<BakedQuad, ExtendedBakedQuad> EXTENDED_BAKED_QUAD = create();
    record ExtendedBakedQuad(BakedNormals bakedNormals, BakedColors bakedColors) {}

    Map<BakedQuad.MaterialInfo, ExtendedBakedQuadMaterialInfo> EXTENDED_BAKED_QUAD_MATERIAL_INFO = create();
    record ExtendedBakedQuadMaterialInfo(boolean ambientOcclusion) {}

    Map<CuboidFace, ExtendedCuboidFace> EXTENDED_CUBOID_FACE = create();
    record ExtendedCuboidFace(@Nullable ExtraFaceData faceData, MutableObject<CuboidModelElement> parent) {}

    Map<CuboidModelElement, ExtendedCuboidModelElement> EXTENDED_CUBOID_MODEL_ELEMENT = create();
    record ExtendedCuboidModelElement(ExtraFaceData faceData) {}

    Map<RenderPipeline.Snippet, ExtendedRenderPipelineSnippet> EXTENDED_RENDER_PIPELINE_SNIPPET = create();
    record ExtendedRenderPipelineSnippet(Optional<StencilTest> stencilTest) {}

    static <K, V> Map<K, V> create() {
        return RecordMixinRefMaps.create();
    }
}
