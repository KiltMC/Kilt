package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = MultiPartBakedModel.class, priority = 1050)
public abstract class MultiPartBakedModelInject implements IDynamicBakedModel {
    @Shadow public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random);

    @Shadow @Final private Map<BlockState, BitSet> selectorCache;
    @Shadow @Final private List<Pair<Predicate<BlockState>, BakedModel>> selectors;
    @Unique private final ThreadLocal<RenderType> kilt$currentRenderType = new ThreadLocal<>();
    @Unique private final ThreadLocal<ModelData> kilt$currentModelData = new ThreadLocal<>();

    @Unique
    private BakedModel defaultModel;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initDefaultModel(List selectors, CallbackInfo ci, @Local BakedModel bakedModel) {
        this.defaultModel = bakedModel;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource randomSource, ModelData modelData, @Nullable RenderType renderType) {
        kilt$currentRenderType.set(renderType);
        kilt$currentModelData.set(modelData);

        try {
            return this.getQuads(state, face, randomSource);
        } finally {
            kilt$currentModelData.remove();
            kilt$currentRenderType.remove();
        }
    }

    @WrapOperation(method = "getQuads", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$addAllForgeQuads(BakedModel instance, @Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original) {
        if (kilt$currentRenderType.get() == null)
            return original.call(instance, blockState, direction, randomSource);

        if (instance.getRenderTypes(blockState, randomSource, kilt$currentModelData.get()).contains(kilt$currentRenderType.get())) {
            return instance.getQuads(blockState, direction, randomSource, kilt$currentModelData.get(), kilt$currentRenderType.get());
        }

        return List.of();
    }

    public BitSet getSelectors(@Nullable BlockState state) {
        BitSet bitSet = this.selectorCache.get(state);
        if (bitSet == null) {
            bitSet = new BitSet();

            for (int i = 0; i < this.selectors.size(); i++) {
                Pair<Predicate<BlockState>, BakedModel> pair = this.selectors.get(i);
                if (pair.getLeft().test(state)) {
                    bitSet.set(i);
                }
            }

            this.selectorCache.put(state, bitSet);
        }

        return bitSet;
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return this.defaultModel.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return this.defaultModel.useAmbientOcclusion(state, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.defaultModel.getParticleIcon(data);
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return this.defaultModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        var renderTypeSets = new LinkedList<ChunkRenderTypeSet>();
        var selectors = this.getSelectors(state);

        for (int i = 0; i < selectors.length(); i++)
            if (selectors.get(i))
                renderTypeSets.add(this.selectors.get(i).getRight().getRenderTypes(state, rand, data));

        return ChunkRenderTypeSet.union(renderTypeSets);
    }
}
