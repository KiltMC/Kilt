package xyz.bluspring.kilt.injects.client.renderer;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.client.KiltClient;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection;
import xyz.bluspring.kilt.mixin.ItemBlockRenderTypesAccessor;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

@Mixin(ItemBlockRenderTypes.class)
public abstract class ItemBlockRenderTypesInject implements ItemBlockRenderTypesInjection {
    @CreateStatic private static final ChunkRenderTypeSet CUTOUT_MIPPED = ItemBlockRenderTypesInjection.CUTOUT_MIPPED;
    @CreateStatic private static final ChunkRenderTypeSet SOLID = ItemBlockRenderTypesInjection.SOLID;

    @CreateStatic private static final Map<Block, ChunkRenderTypeSet> BLOCK_RENDER_TYPES = ItemBlockRenderTypesInjection.BLOCK_RENDER_TYPES;

    @CreateStatic
    private static ChunkRenderTypeSet getRenderLayers(BlockState state) {
        if (state.getBlock() instanceof LeavesBlock) {
            return ItemBlockRenderTypesAccessor.isRenderCutout() ? CUTOUT_MIPPED : SOLID;
        }

        // Kilt: Handle Fabric mods' render types
        if (!BLOCK_RENDER_TYPES.containsKey(state.getBlock())) {
            BLOCK_RENDER_TYPES.put(state.getBlock(), ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state)));
        }

        return BLOCK_RENDER_TYPES.get(state.getBlock());
    }

    @CreateStatic
    private static void setRenderLayer(Block block, RenderType type) {
        setRenderLayer(block, ChunkRenderTypeSet.of(type));
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        setRenderLayer(block, ChunkRenderTypeSet.of(RenderType.chunkBufferLayers().stream().filter(predicate).collect(Collectors.toList())));
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Block block, ChunkRenderTypeSet layers) {
        checkClientLoading();
        BlockRenderLayerMap.INSTANCE.putBlock(block, layers.asList().get(0));
        BLOCK_RENDER_TYPES.put(block, layers);
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Fluid fluid, RenderType type) {
        checkClientLoading();
        BlockRenderLayerMap.INSTANCE.putFluid(fluid, type);
    }

    private static void checkClientLoading() {
        Preconditions.checkState(KiltClient.Companion.getHasInitialized(), "Render layers can only be set during client loading!");
    }
}
