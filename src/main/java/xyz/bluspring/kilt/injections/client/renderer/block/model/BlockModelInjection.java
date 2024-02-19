package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;

import java.util.function.Function;

public interface BlockModelInjection {
    ResourceLocation getParentLocation();
    BlockGeometryBakingContext kilt$getCustomData();
    ItemOverrides getOverrides(ModelBaker baker, BlockModel blockModel, Function<Material, TextureAtlasSprite> spriteGetter);
    String getSerializedName();
}
