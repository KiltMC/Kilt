// TRACKED HASH: 2ea7001a7c90b80f52417cae8858b9c1cbe2be58
package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(WeightedBakedModel.class)
public abstract class WeightedBakedModelInject implements IDynamicBakedModel {
    @Shadow public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random);

    @Shadow @Final private BakedModel wrapped;
    @Shadow @Final private List<WeightedEntry.Wrapper<BakedModel>> list;
    @Shadow @Final private int totalWeight;
    @Unique private static final ThreadLocal<RenderType> kilt$renderType = ThreadLocal.withInitial(() -> null);
    @Unique private static final ThreadLocal<ModelData> kilt$modelData = ThreadLocal.withInitial(() -> ModelData.EMPTY);

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        kilt$renderType.set(renderType);
        kilt$modelData.set(extraData);

        var result = this.getQuads(state, side, rand);

        kilt$modelData.remove();
        kilt$renderType.remove();

        return result;
    }

    @WrapOperation(method = "method_33461", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private static List<BakedQuad> kilt$useForgeQuadGetterIfAvailable(BakedModel instance, BlockState blockState, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original) {
        var renderType = kilt$renderType.get();
        var modelData = kilt$modelData.get();

        if (renderType == null && modelData.equals(ModelData.EMPTY)) {
            // Defer back to original call, in case some other mods are relying on this behaviour.
            return original.call(instance, blockState, direction, randomSource);
        }

        return instance.getQuads(blockState, direction, randomSource, modelData, renderType);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return this.wrapped.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return this.wrapped.useAmbientOcclusion(state, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.wrapped.getParticleIcon(data);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return this.wrapped.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return WeightedRandom.getWeightedItem(this.list, Math.abs((int) rand.nextLong()) % this.totalWeight)
            .map(modelWrapper -> modelWrapper.getData().getRenderTypes(state, rand, data))
            .orElse(ChunkRenderTypeSet.none());
    }
}