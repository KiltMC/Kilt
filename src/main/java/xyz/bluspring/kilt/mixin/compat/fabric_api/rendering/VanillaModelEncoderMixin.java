package xyz.bluspring.kilt.mixin.compat.fabric_api.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.FRAPIThreadedStorage;
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection;

import java.util.LinkedList;
import java.util.List;

@Mixin(VanillaModelEncoder.class)
public class VanillaModelEncoderMixin {
    @Shadow(remap = false) @Final private static Renderer RENDERER;

    @WrapOperation(method = "emitBlockQuads", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private static List<BakedQuad> kilt$useForgeEmitQuads(BakedModel model, BlockState state, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original, @Share("renderTypes") LocalRef<List<RenderType>> mappedRenderTypes) {
        var level = FRAPIThreadedStorage.LEVEL.get();
        var pos = FRAPIThreadedStorage.POS.get();

        // Make sure we actually have the things for it
        if (level == null || pos == null)
            return original.call(model, state, direction, randomSource);

        ModelDataManager modelDataManager = level.getModelDataManager();

        // apparently this occurs
        if (modelDataManager == null)
            return original.call(model, state, direction, randomSource);

        var modelData = model.getModelData(level, pos, state, modelDataManager.getAt(new ChunkPos(pos)).getOrDefault(pos, ModelData.EMPTY));
        var renderTypes = model.getRenderTypes(state, randomSource, modelData);

        if (modelData == ModelData.EMPTY && (renderTypes.isEmpty() ||
            // TODO: this may cause performance issues, and might not actually be what we want, so.
            ChunkRenderTypeSet.intersection(ItemBlockRenderTypesInjection.getRenderLayers(state), renderTypes).asList().size() == renderTypes.asList().size())
        )
            return original.call(model, state, direction, randomSource);

        mappedRenderTypes.set(new LinkedList<>());
        var list = new LinkedList<BakedQuad>();

        for (RenderType renderType : renderTypes) {
            var quads = model.getQuads(state, direction, randomSource, modelData, renderType);
            list.addAll(quads);

            // TODO: optimize?
            for (int i = 0; i < quads.size(); i++) {
                mappedRenderTypes.get().add(renderType);
            }
        }

        return list;
    }

    @ModifyArg(method = "emitBlockQuads", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"))
    private static RenderMaterial kilt$useMappedRenderType(RenderMaterial material, @Share("renderTypes") LocalRef<List<RenderType>> mappedRenderTypes, @Local(ordinal = 2) int j) {
        var mapped = mappedRenderTypes.get();
        if (mapped == null || mapped.isEmpty())
            return material;

        if (j >= mapped.size())
            mappedRenderTypes.set(null);

        var renderType = mapped.get(j);

        // TODO: figure out a way to check all other values
        return RENDERER.materialFinder()
            .blendMode(BlendMode.fromRenderLayer(renderType))
            .disableColorIndex(!renderType.format().getElementAttributeNames().contains("Color"))
            .disableDiffuse(!renderType.format().getElementAttributeNames().contains("UV0"))
            .find();
    }
}
