package xyz.bluspring.kilt.injects.client.resources.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryInject implements ModelBakeryInjection {
    @Unique private StandaloneModelLoader.LoadedModels standaloneModels = StandaloneModelLoader.LoadedModels.EMPTY;
    @Unique private AnimationLoader.PendingAnimations pendingAnimations = AnimationLoader.PendingAnimations.EMPTY;

    @Shadow
    public ModelBakeryInject(final EntityModelSet entityModelSet, final SpriteGetter sprites, final PlayerSkinRenderCache playerSkinRenderCache, final Map<BlockState, BlockStateModel.UnbakedRoot> unbakedBlockStateModels, final Map<Identifier, ClientItem> clientInfos, final Map<Identifier, ResolvedModel> resolvedModels, final ResolvedModel missingModel) {}

    @CreateInitializer
    public ModelBakeryInject(final EntityModelSet entityModelSet, final SpriteGetter sprites, final PlayerSkinRenderCache playerSkinRenderCache, final Map<BlockState, BlockStateModel.UnbakedRoot> unbakedBlockStateModels, final Map<Identifier, ClientItem> clientInfos, final Map<Identifier, ResolvedModel> resolvedModels, final ResolvedModel missingModel, StandaloneModelLoader.LoadedModels standaloneModels, AnimationLoader.PendingAnimations pendingAnimations) {
        this(entityModelSet, sprites, playerSkinRenderCache, unbakedBlockStateModels, clientInfos, resolvedModels, missingModel);
        this.standaloneModels = standaloneModels;
        this.pendingAnimations = pendingAnimations;
    }

    @ModifyExpressionValue(method = "lambda$bakeModels$1", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/model/geom/EntityModelSet;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;Lnet/minecraft/client/renderer/PlayerSkinRenderCache;Lnet/minecraft/client/renderer/item/MissingItemModel;Lnet/minecraft/util/RegistryContextSwapper;)Lnet/minecraft/client/renderer/item/ItemModel$BakingContext;"))
    private ItemModel.BakingContext kilt$attachPendingAnimations(ItemModel.BakingContext original) {
        ClientRecordMixinRefMaps.EXTENDED_ITEM_MODEL_BAKING_CONTEXT.put(original, new ClientRecordMixinRefMaps.ExtendedItemModelBakingContext(this.pendingAnimations));
        return original;
    }

    @Definition(id = "itemStackModelProperties", local = @Local(type = Map.class, name = "itemStackModelProperties"))
    @Definition(id = "HashMap", type = HashMap.class)
    @Expression("itemStackModelProperties = new HashMap(?)")
    @Inject(method = "bakeModels", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$createStandaloneModelsFuture(MaterialBaker materials, Executor taskExecutor, CallbackInfoReturnable<CompletableFuture<ModelBakery.BakingResult>> cir, @Share("standaloneModelsFuture") LocalRef<CompletableFuture<StandaloneModelLoader.BakedModels>> standaloneModelsFuture, @Local(name = "baker") ModelBakery.ModelBakerImpl baker) {
        standaloneModelsFuture.set(StandaloneModelLoader.bake(this.standaloneModels, baker, taskExecutor));
    }

    @Unique private static final ThreadLocal<CompletableFuture<StandaloneModelLoader.BakedModels>> kilt$standaloneModelsFuture = new ThreadLocal<>();

    @ModifyReceiver(method = "bakeModels", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCombine(Ljava/util/concurrent/CompletionStage;Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private <T, U, V> CompletableFuture<Map<BlockState, BlockStateModel>> kilt$modifyBakedBlockStateFuture(CompletableFuture<Map<BlockState, BlockStateModel>> instance, CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, @Share("standaloneModelsFuture") LocalRef<CompletableFuture<StandaloneModelLoader.BakedModels>> standaloneModelsFuture) {
        kilt$standaloneModelsFuture.set(standaloneModelsFuture.get());
        return instance.thenCombine(standaloneModelsFuture.get(), (stateModels, _) -> stateModels);
    }

    @ModifyExpressionValue(method = "lambda$bakeModels$3", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBakery$MissingModels;Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;)Lnet/minecraft/client/resources/model/ModelBakery$BakingResult;"))
    private static ModelBakery.BakingResult kilt$attachStandaloneModelsFuture(ModelBakery.BakingResult original) {
        var future = kilt$standaloneModelsFuture.get();
        if (future != null) {
            ClientRecordMixinRefMaps.EXTENDED_MODEL_BAKERY_BAKING_RESULT.put(original, new ClientRecordMixinRefMaps.ExtendedModelBakeryBakingResult(future.join()));
        }

        return original;
    }

    @Mixin(ModelBakery.BakingResult.class)
    public abstract static class BakingResultInject implements ModelBakeryInjection.BakingResultInjection {
        @Shadow
        public BakingResultInject(ModelBakery.MissingModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<Identifier, ItemModel> itemStackModels, Map<Identifier, ClientItem.Properties> itemProperties) {}

        @CreateInitializer
        public BakingResultInject(ModelBakery.MissingModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<Identifier, ItemModel> itemStackModels, Map<Identifier, ClientItem.Properties> itemProperties, StandaloneModelLoader.BakedModels standaloneModels) {
            this(missingModels, blockStateModels, itemStackModels, itemProperties);
            ClientRecordMixinRefMaps.EXTENDED_MODEL_BAKERY_BAKING_RESULT.put((ModelBakery.BakingResult) (Object) this, new ClientRecordMixinRefMaps.ExtendedModelBakeryBakingResult(standaloneModels));
        }

        @Override
        public StandaloneModelLoader.BakedModels standaloneModels() {
            var extension = ClientRecordMixinRefMaps.EXTENDED_MODEL_BAKERY_BAKING_RESULT.get(this);
            if (extension != null)
                return extension.standaloneModels();

            return StandaloneModelLoader.BakedModels.EMPTY;
        }
    }

    @Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$InternerImpl")
    public abstract static class InternerImpl implements ModelBaker.Interner {
        @Unique private final Interner<BakedNormals> normals = Interners.newStrongInterner();
        @Unique private final Interner<BakedColors> colors = Interners.newStrongInterner();

        @Override
        public BakedNormals normals(BakedNormals normals) {
            return this.normals.intern(normals);
        }

        @Override
        public BakedColors colors(BakedColors colors) {
            return this.colors.intern(colors);
        }
    }
}
