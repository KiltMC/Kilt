package xyz.bluspring.kilt.helpers;

import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
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

    Map<ItemModelGenerator.ItemLayerKey, ExtendedItemLayerKey> EXTENDED_ITEM_LAYER_KEY = create();
    record ExtendedItemLayerKey(ExtraFaceData faceData) {}

    Map<ItemTransform, ExtendedItemTransform> EXTENDED_ITEM_TRANSFORM = create();
    record ExtendedItemTransform(Vector3fc rightRotation) {}

    Map<BlockStateModelDispatcher, ExtendedBlockStateModelDispatcher> EXTENDED_BLOCK_STATE_MODEL_DISPATCHER = create();
    record ExtendedBlockStateModelDispatcher(Optional<CustomBlockModelDefinition> customDefinition) {}

    Map<ModelBakery.BakingResult, ExtendedModelBakeryBakingResult> EXTENDED_MODEL_BAKERY_BAKING_RESULT = create();
    record ExtendedModelBakeryBakingResult(StandaloneModelLoader.BakedModels standaloneModels) {}

    Map<ItemModel.BakingContext, ExtendedItemModelBakingContext> EXTENDED_ITEM_MODEL_BAKING_CONTEXT = create();
    record ExtendedItemModelBakingContext(AnimationLoader.PendingAnimations pendingAnimations) {}

    static <K, V> Map<K, V> create() {
        return RecordMixinRefMaps.create();
    }
}
