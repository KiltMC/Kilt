package xyz.bluspring.kilt.injections.client.resources.model;

import java.util.Map;

import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public interface ModelBakeryInjection {
    default void kilt$setStandaloneModels(StandaloneModelLoader.LoadedModels models) {
        throw KiltHelper.createMixinException(ModelBakeryInjection.class, "kilt$setStandaloneModels");
    }

    default void kilt$setPendingAnimations(AnimationLoader.PendingAnimations pendingAnimations) {
        throw KiltHelper.createMixinException(ModelBakeryInjection.class, "kilt$setPendingAnimations");
    }

    static ModelBakery create(final EntityModelSet entityModelSet, final SpriteGetter sprites, final PlayerSkinRenderCache playerSkinRenderCache, final Map<BlockState, BlockStateModel.UnbakedRoot> unbakedBlockStateModels, final Map<Identifier, ClientItem> clientInfos, final Map<Identifier, ResolvedModel> resolvedModels, final ResolvedModel missingModel, StandaloneModelLoader.LoadedModels standaloneModels, AnimationLoader.PendingAnimations pendingAnimations) {
        var bakery = new ModelBakery(entityModelSet, sprites, playerSkinRenderCache, unbakedBlockStateModels, clientInfos, resolvedModels, missingModel);
        bakery.kilt$setPendingAnimations(pendingAnimations);
        bakery.kilt$setStandaloneModels(standaloneModels);

        return bakery;
    }

    interface BakingResultInjection {
        default StandaloneModelLoader.BakedModels standaloneModels() {
            throw KiltHelper.createMixinException(BakingResultInjection.class, "standaloneModels");
        }

        static ModelBakery.BakingResult create(ModelBakery.MissingModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<Identifier, ItemModel> itemStackModels, Map<Identifier, ClientItem.Properties> itemProperties, StandaloneModelLoader.BakedModels standaloneModels) {
            var result = new ModelBakery.BakingResult(missingModels, blockStateModels, itemStackModels, itemProperties);
            ClientRecordMixinRefMaps.EXTENDED_MODEL_BAKERY_BAKING_RESULT.put(result, new ClientRecordMixinRefMaps.ExtendedModelBakeryBakingResult(standaloneModels));
            return result;
        }
    }
}
