package xyz.bluspring.kilt.forgeinjects.client.resources.model;

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
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.resources.model.SimpleBakedModelBuilderInjection;
import xyz.bluspring.kilt.injections.client.resources.model.SimpleBakedModelInjection;

import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelInject implements BakedModelInject, SimpleBakedModelInjection {
    protected ChunkRenderTypeSet blockRenderTypes;
    protected List<RenderType> itemRenderTypes;
    protected List<RenderType> fabulousItemRenderTypes;

    @Override
    public void addRenderTypes(RenderTypeGroup renderTypeGroup) {
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
    public static class BuilderInject implements SimpleBakedModelBuilderInjection {
        @Shadow private TextureAtlasSprite particleIcon;

        @Shadow @Final private List<BakedQuad> unculledFaces;

        @Shadow @Final private Map<Direction, List<BakedQuad>> culledFaces;

        @Shadow @Final private boolean hasAmbientOcclusion;

        @Shadow @Final private boolean usesBlockLight;

        @Shadow @Final private boolean isGui3d;

        @Shadow @Final private ItemTransforms transforms;

        @Shadow @Final private ItemOverrides overrides;

        @Override
        public BakedModel build(RenderTypeGroup renderTypeGroup) {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                return SimpleBakedModelInjection.create(this.unculledFaces, this.culledFaces, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides, renderTypeGroup);
            }
        }
    }
}
