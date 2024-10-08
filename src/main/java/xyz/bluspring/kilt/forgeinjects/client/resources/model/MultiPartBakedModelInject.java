package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.MultipartModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.resources.model.MultiPartBakedModelInjection;

import java.util.*;
import java.util.function.Predicate;

@Mixin(value = MultiPartBakedModel.class, priority = 1050)
public abstract class MultiPartBakedModelInject implements IDynamicBakedModel, MultiPartBakedModelInjection {
    @Shadow @Final private Map<BlockState, BitSet> selectorCache;
    @Shadow @Final private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

    @Shadow public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random);

    @Unique private BakedModel defaultModel;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getRight()Ljava/lang/Object;"))
    private <L, R> R kilt$setDefaultModel(Pair<L, R> instance, Operation<R> original) {
        var model = original.call(instance);
        this.defaultModel = (BakedModel) model;

        return model;
    }

    @Override
    public BitSet getSelectors(@Nullable BlockState state) {
        BitSet bitSet = this.selectorCache.get(state);

        if (bitSet == null) {
            bitSet = new BitSet();

            for(int i = 0; i < this.selectors.size(); ++i) {
                Pair<Predicate<BlockState>, BakedModel> pair = this.selectors.get(i);
                if (pair.getLeft().test(state)) {
                    bitSet.set(i);
                }
            }

            this.selectorCache.put(state, bitSet);
        }

        return bitSet;
    }

    @Unique private final ThreadLocal<RenderType> kilt$renderType = ThreadLocal.withInitial(() -> null);
    @Unique private final ThreadLocal<ModelData> kilt$modelData = ThreadLocal.withInitial(() -> ModelData.EMPTY);

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource randomSource, ModelData modelData, @Nullable RenderType renderType) {
        if (state == null)
            return Collections.emptyList();

        kilt$renderType.set(renderType);
        kilt$modelData.set(modelData);

        var result = this.getQuads(state, direction, randomSource);

        kilt$modelData.remove();
        kilt$renderType.remove();

        return result;
    }

    @IfModAbsent("sodium")
    @WrapWithCondition(method = "getQuads", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean kilt$useQuadDataIfAvailable(List<BakedQuad> instance, Collection<BakedQuad> es, @Local int j, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) RandomSource randomSource, @Share("model") LocalRef<BakedModel> model) {
        model.set(this.selectors.get(j).getRight());
        return kilt$renderType.get() == null || model.get().getRenderTypes(state, randomSource, kilt$modelData.get()).contains(kilt$renderType.get());
    }

    @IfModAbsent("sodium")
    @WrapOperation(method = "getQuads", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$useForgeQuadGetterIfAvailable(BakedModel instance, BlockState blockState, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original, @Local long l) {
        var renderType = kilt$renderType.get();
        var modelData = kilt$modelData.get();

        if (renderType == null && modelData.equals(ModelData.EMPTY)) {
            // Defer back to original call in this case, in case some other mods are relying on this behaviour.
            return original.call(instance, blockState, direction, randomSource);
        }

        return instance.getQuads(blockState, direction, RandomSource.create(l), MultipartModelData.resolve(modelData, instance), renderType);
    }

    // Sodium 0.5.x support
    @SuppressWarnings("UnresolvedMixinReference")
    @IfModAbsent(value = "sodium", minVersion = "0.6.0")
    @IfModLoaded("sodium")
    @TargetHandler(
        mixin = "me.jellysquid.mods.sodium.mixin.features.model.MultipartBakedModelMixin",
        name = "getQuads"
    )
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean kilt$useQuadDataIfAvailableSodium05(List<BakedQuad> instance, Collection<BakedQuad> es, @Local BakedModel model, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) RandomSource randomSource) {
        return kilt$renderType.get() == null || model.getRenderTypes(state, randomSource, kilt$modelData.get()).contains(kilt$renderType.get());
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @IfModAbsent(value = "sodium", minVersion = "0.6.0")
    @IfModLoaded("sodium")
    @TargetHandler(
        mixin = "me.jellysquid.mods.sodium.mixin.features.model.MultipartBakedModelMixin",
        name = "getQuads"
    )
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$useForgeQuadGetterIfAvailableSodium05(BakedModel instance, BlockState blockState, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original, @Local(ordinal = 1) long seed) {
        var renderType = kilt$renderType.get();
        var modelData = kilt$modelData.get();

        if (renderType == null && modelData.equals(ModelData.EMPTY)) {
            // Defer back to original call in this case, in case some other mods are relying on this behaviour.
            return original.call(instance, blockState, direction, randomSource);
        }

        // Sodium sets the seed already for us, we don't have to create a new random.
        return instance.getQuads(blockState, direction, randomSource, MultipartModelData.resolve(modelData, instance), renderType);
    }

    // Sodium 0.6.x support
    @SuppressWarnings("UnresolvedMixinReference")
    @IfModLoaded(value = "sodium", minVersion = "0.6.0")
    @TargetHandler(
        mixin = "net.caffeinemc.mods.sodium.mixin.features.model.MultipartBakedModelMixin",
        name = "getQuads"
    )
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean kilt$useQuadDataIfAvailableSodium06(List<BakedQuad> instance, Collection<BakedQuad> es, @Local BakedModel model, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) RandomSource randomSource) {
        return kilt$renderType.get() == null || model.getRenderTypes(state, randomSource, kilt$modelData.get()).contains(kilt$renderType.get());
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @IfModLoaded(value = "sodium", minVersion = "0.6.0")
    @TargetHandler(
        mixin = "net.caffeinemc.mods.sodium.mixin.features.model.MultipartBakedModelMixin",
        name = "getQuads"
    )
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$useForgeQuadGetterIfAvailableSodium06(BakedModel instance, BlockState blockState, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original, @Local(ordinal = 1) long seed) {
        var renderType = kilt$renderType.get();
        var modelData = kilt$modelData.get();

        if (renderType == null && modelData.equals(ModelData.EMPTY)) {
            // Defer back to original call in this case, in case some other mods are relying on this behaviour.
            return original.call(instance, blockState, direction, randomSource);
        }

        // Sodium sets the seed already for us, we don't have to create a new random.
        return instance.getQuads(blockState, direction, randomSource, MultipartModelData.resolve(modelData, instance), renderType);
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
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return MultipartModelData.create(selectors, this.getSelectors(state), level, pos, state, modelData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.defaultModel.getParticleIcon(data);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return this.defaultModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        var renderTypeSets = new LinkedList<ChunkRenderTypeSet>();
        var selectors = this.getSelectors(state);

        for (int i = 0; i < selectors.length(); i++) {
            if (selectors.get(i))
                renderTypeSets.add(this.selectors.get(i).getRight().getRenderTypes(state, rand, data));
        }

        return ChunkRenderTypeSet.union(renderTypeSets);
    }
}
