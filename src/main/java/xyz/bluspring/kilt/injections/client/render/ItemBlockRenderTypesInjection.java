package xyz.bluspring.kilt.injections.client.render;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.bluspring.kilt.client.KiltClient;
import xyz.bluspring.kilt.mixin.ItemBlockRenderTypesAccessor;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface ItemBlockRenderTypesInjection {
    ChunkRenderTypeSet CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    ChunkRenderTypeSet SOLID = ChunkRenderTypeSet.of(RenderType.solid());
    Object2ObjectOpenHashMap<Holder.Reference<Block>, ChunkRenderTypeSet> BLOCK_RENDER_TYPES = Util.make(new Object2ObjectOpenHashMap<>(ItemBlockRenderTypes.TYPE_BY_BLOCK.size(), .5F), (it) -> {
        it.defaultReturnValue(SOLID);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.forEach((key, value) -> {
                it.put(ForgeRegistries.BLOCKS.getDelegateOrThrow(key), ChunkRenderTypeSet.of(value));
        });
    });
    // why does this feel utterly pointless
    Object2ObjectOpenHashMap<Holder.Reference<Fluid>, RenderType> FLUID_RENDER_TYPES = Util.make(new Object2ObjectOpenHashMap<>(ItemBlockRenderTypes.TYPE_BY_FLUID.size(), .5F), (it) -> {
        it.defaultReturnValue(RenderType.solid());
        ItemBlockRenderTypes.TYPE_BY_FLUID.forEach((key, value) -> {
                it.put(ForgeRegistries.FLUIDS.getDelegateOrThrow(key), value);
        });
    });*

    static ChunkRenderTypeSet getRenderLayers(BlockState state) {
        if (state.getBlock() instanceof LeavesBlock) {
            return ItemBlockRenderTypesAccessor.isRenderCutout() ? CUTOUT_MIPPED : SOLID;
        }

        return BLOCK_RENDER_TYPES.get(ForgeRegistries.BLOCKS.getDelegateOrThrow(state.getBlock()));
    }

    static void setRenderLayer(Block block, RenderType type) {
        setRenderLayer(block, ChunkRenderTypeSet.of(type));
    }

    static void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        setRenderLayer(block, ChunkRenderTypeSet.of(RenderType.chunkBufferLayers().stream().filter(predicate).collect(Collectors.toList())));
    }

    static void setRenderLayer(Block block, ChunkRenderTypeSet layers) {
        checkClientLoading();
        BlockRenderLayerMap.INSTANCE.putBlock(block, layers.asList().get(0));
        BLOCK_RENDER_TYPES.put(ForgeRegistries.BLOCKS.getDelegateOrThrow(block), layers);
    }

    static void setRenderLayer(Fluid fluid, RenderType type) {
        checkClientLoading();
        BlockRenderLayerMap.INSTANCE.putFluid(fluid, type);
        FLUID_RENDER_TYPES.put(ForgeRegistries.FLUIDS.getDelegateOrThrow(fluid), type);
    }

    static void checkClientLoading() {
        Preconditions.checkState(KiltClient.Companion.getHasInitialized(), "Render layers can only be set during client loading!");
    }
}
