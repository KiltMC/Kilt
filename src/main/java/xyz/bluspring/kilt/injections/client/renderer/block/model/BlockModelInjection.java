package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.BlockGeometryBakingContext;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Function;

public interface BlockModelInjection {
    default ResourceLocation getParentLocation() {
        throw KiltHelper.createMixinException(BlockModelInjection.class, "getParentLocation");
    }

    default BlockGeometryBakingContext kilt$getCustomData() {
        throw KiltHelper.createMixinException(BlockModelInjection.class, "kilt$getCustomData");
    }

    default ItemOverrides getOverrides(ModelBaker baker, BlockModel blockModel, Function<Material, TextureAtlasSprite> spriteGetter) {
        throw KiltHelper.createMixinException(BlockModelInjection.class, "getOverrides");
    }

    default String getSerializedName() {
        throw KiltHelper.createMixinException(BlockModelInjection.class, "getSerializedName");
    }
}
