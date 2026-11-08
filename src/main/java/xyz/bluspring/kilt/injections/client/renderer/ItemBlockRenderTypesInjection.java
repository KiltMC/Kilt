package xyz.bluspring.kilt.injections.client.renderer;

import java.util.Map;
import java.util.function.Predicate;

import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import xyz.bluspring.kilt.helpers.FabricImplemented;
import xyz.bluspring.kilt.util.DefaultedHashMap;

import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@FabricImplemented(ItemBlockRenderTypes.class)
public interface ItemBlockRenderTypesInjection {
    ChunkRenderTypeSet CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    ChunkRenderTypeSet SOLID = ChunkRenderTypeSet.of(RenderType.solid());
    Map<Block, ChunkRenderTypeSet> BLOCK_RENDER_TYPES = Util.make(new DefaultedHashMap<>(ItemBlockRenderTypes.TYPE_BY_BLOCK.size(), .5F), (it) -> {
        it.setDefaultValue(SOLID);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.forEach((key, value) -> {
                it.put(key, ChunkRenderTypeSet.of(value));
        });
    });

    static ChunkRenderTypeSet getRenderLayers(BlockState state) {
        throw new AssertionError();
    }

    static void setRenderLayer(Block block, RenderType type) {
        throw new AssertionError();
    }

    static void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        throw new AssertionError();
    }

    static void setRenderLayer(Block block, ChunkRenderTypeSet layers) {
        throw new AssertionError();
    }

    static void setRenderLayer(Fluid fluid, RenderType type) {
        throw new AssertionError();
    }
}
