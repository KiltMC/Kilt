// TRACKED HASH: 25378fba48cfc872af48b0db6f756b86dead555a
package xyz.bluspring.kilt.injects.client.resources.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.resources.model.SimpleBakedModelBuilderInjection;
import xyz.bluspring.kilt.injections.client.resources.model.SimpleBakedModelInjection;

import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public abstract class SimpleBakedModelInject implements BakedModelInject, SimpleBakedModelInjection {
    protected ChunkRenderTypeSet blockRenderTypes;
    protected List<RenderType> itemRenderTypes;
    protected List<RenderType> fabulousItemRenderTypes;

    public SimpleBakedModelInject(List<BakedQuad> unculledFaces, Map<Direction, List<BakedQuad>> culledFaces, boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, TextureAtlasSprite particleIcon, ItemTransforms transforms, ItemOverrides overrides) {}

    @CreateInitializer
    public SimpleBakedModelInject(List<BakedQuad> unculledFaces, Map<Direction, List<BakedQuad>> culledFaces, boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, TextureAtlasSprite particleIcon, ItemTransforms transforms, ItemOverrides overrides, RenderTypeGroup renderTypeGroup) {
        this(unculledFaces, culledFaces, hasAmbientOcclusion, usesBlockLight, isGui3d, particleIcon, transforms, overrides);
        this.kilt$addRenderTypes(renderTypeGroup);
    }

    @Override
    public void kilt$addRenderTypes(RenderTypeGroup renderTypeGroup) {
        if (renderTypeGroup == null)
            renderTypeGroup = RenderTypeGroup.EMPTY;

        this.blockRenderTypes = !renderTypeGroup.isEmpty() ? ChunkRenderTypeSet.of(renderTypeGroup.block()) : null;
        this.itemRenderTypes = !renderTypeGroup.isEmpty() ? List.of(renderTypeGroup.entity()) : null;
        this.fabulousItemRenderTypes = !renderTypeGroup.isEmpty() ? List.of(renderTypeGroup.entityFabulous()) : null;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        if (blockRenderTypes != null)
            return blockRenderTypes;

        return BakedModelInject.super.getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        if (!fabulous)
            if (itemRenderTypes != null)
                return itemRenderTypes;
        else if (fabulousItemRenderTypes != null)
            return fabulousItemRenderTypes;

        return BakedModelInject.super.getRenderTypes(itemStack, fabulous);
    }

    @Mixin(SimpleBakedModel.Builder.class)
    public static abstract class BuilderInject implements SimpleBakedModelBuilderInjection {
        @Shadow public abstract BakedModel build();

        @Override
        public BakedModel build(RenderTypeGroup renderTypeGroup) {
            var model = this.build();
            ((SimpleBakedModelInjection) model).kilt$addRenderTypes(renderTypeGroup);

            return model;
        }
    }
}