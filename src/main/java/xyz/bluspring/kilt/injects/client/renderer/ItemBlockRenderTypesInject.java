package xyz.bluspring.kilt.injects.client.renderer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(ItemBlockRenderTypes.class)
public abstract class ItemBlockRenderTypesInject implements ItemBlockRenderTypesInjection {
    @CreateStatic private static final ChunkRenderTypeSet CUTOUT_MIPPED = ItemBlockRenderTypesInjection.CUTOUT_MIPPED;
    @CreateStatic private static final ChunkRenderTypeSet SOLID = ItemBlockRenderTypesInjection.SOLID;

    @CreateStatic private static final Map<Block, ChunkRenderTypeSet> BLOCK_RENDER_TYPES = ItemBlockRenderTypesInjection.BLOCK_RENDER_TYPES;

    @CreateStatic
    private static ChunkRenderTypeSet getRenderLayers(BlockState state) {
        return ItemBlockRenderTypesInjection.getRenderLayers(state);
    }

    @CreateStatic
    private static void setRenderLayer(Block block, RenderType renderType) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, renderType);
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, predicate);
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Block block, ChunkRenderTypeSet layers) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, layers);
    }

    @CreateStatic
    private static synchronized void setRenderLayer(Fluid fluid, RenderType type) {
        ItemBlockRenderTypesInjection.setRenderLayer(fluid, type);
    }
}
