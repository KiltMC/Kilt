package xyz.bluspring.kilt.mixin.compat.fabric_api.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.FRAPIThreadedStorage;

import java.util.LinkedList;
import java.util.List;

@Mixin(VanillaModelEncoder.class)
public class VanillaModelEncoderMixin {
    @WrapOperation(method = "emitBlockQuads", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private static List<BakedQuad> kilt$useForgeEmitQuads(BakedModel model, BlockState state, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original) {
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

        var list = new LinkedList<BakedQuad>();

        for (RenderType renderType : renderTypes) {
            list.addAll(model.getQuads(state, direction, randomSource, modelData, renderType));
        }
        return list;
    }
}
