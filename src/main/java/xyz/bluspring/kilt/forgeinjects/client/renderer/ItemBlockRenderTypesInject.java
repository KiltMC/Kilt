// TRACKED HASH: 447c2971f05b4f59ff0d6999b808ec34f403b802
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection;

import java.util.function.Predicate;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesInject implements ItemBlockRenderTypesInjection {
    @CreateStatic
    private static ChunkRenderTypeSet CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped());

    @CreateStatic
    private static ChunkRenderTypeSet SOLID = ChunkRenderTypeSet.of(RenderType.solid());

    @CreateStatic
    private static Object2ObjectOpenHashMap<Holder.Reference<Block>, ChunkRenderTypeSet> BLOCK_RENDER_TYPES = ItemBlockRenderTypesInjection.BLOCK_RENDER_TYPES;

    // why does this feel utterly pointless
    @CreateStatic
    private static Object2ObjectOpenHashMap<Holder.Reference<Fluid>, RenderType> FLUID_RENDER_TYPES = ItemBlockRenderTypesInjection.FLUID_RENDER_TYPES;

    @CreateStatic
    private static ChunkRenderTypeSet getRenderLayers(BlockState state) {
        return ItemBlockRenderTypesInjection.getRenderLayers(state);
    }

    @CreateStatic
    private static void setRenderLayer(Block block, RenderType type) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, type);
    }

    @CreateStatic
    private static void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, predicate);
    }

    @CreateStatic
    private static void setRenderLayer(Block block, ChunkRenderTypeSet layers) {
        ItemBlockRenderTypesInjection.setRenderLayer(block, layers);
    }

    @CreateStatic
    private static void setRenderLayer(Fluid fluid, RenderType type) {
        ItemBlockRenderTypesInjection.setRenderLayer(fluid, type);
    }

    @CreateStatic
    private static void checkClientLoading() {
        ItemBlockRenderTypesInjection.checkClientLoading();
    }
}