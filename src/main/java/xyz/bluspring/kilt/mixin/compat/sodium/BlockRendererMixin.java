package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.compat.sodium.ChunkModelRenderTypeHolder;

import java.util.List;

@IfModLoaded("sodium")
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {
    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$getQuadsByRenderType(BakedModel instance, BlockState blockState, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) ChunkModelBuilder buffers, @Local(argsOnly = true, ordinal = 0) BlockPos pos) {
        var renderType = ((ChunkModelRenderTypeHolder) buffers).kilt$getRenderType();
        var modelData = level.getModelDataManager().getAt(pos);

        if (renderType == null && (modelData == null || modelData == ModelData.EMPTY)) {
            return original.call(instance, blockState, direction, randomSource);
        }

        return instance.getQuads(blockState, direction, randomSource, modelData != null ? modelData : ModelData.EMPTY, renderType);
    }
}
