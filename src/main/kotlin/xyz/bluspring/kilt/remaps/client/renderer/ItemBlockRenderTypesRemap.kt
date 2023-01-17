package xyz.bluspring.kilt.remaps.client.renderer

import com.google.common.base.Preconditions
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.Util
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Holder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.client.ChunkRenderTypeSet
import net.minecraftforge.registries.ForgeRegistries
import xyz.bluspring.kilt.client.KiltClient
import xyz.bluspring.kilt.mixin.ItemBlockRenderTypesAccessor
import java.util.function.Predicate

object ItemBlockRenderTypesRemap {
    internal val CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped())
    internal val SOLID = ChunkRenderTypeSet.of(RenderType.solid())
    internal val BLOCK_RENDER_TYPES = Util.make(Object2ObjectOpenHashMap<Holder.Reference<Block>, ChunkRenderTypeSet>(ItemBlockRenderTypesAccessor.getTypeByBlock().size, .5F)) {
        it.defaultReturnValue(SOLID)
        ItemBlockRenderTypesAccessor.getTypeByBlock().forEach { entry ->
            it[ForgeRegistries.BLOCKS.getDelegateOrThrow(entry.key)] = ChunkRenderTypeSet.of(entry.value)
        }
    }
    // why does this feel utterly pointless
    internal val FLUID_RENDER_TYPES = Util.make(Object2ObjectOpenHashMap<Holder.Reference<Fluid>, RenderType>(ItemBlockRenderTypesAccessor.getTypeByFluid().size, .5F)) {
        it.defaultReturnValue(RenderType.solid())
        ItemBlockRenderTypesAccessor.getTypeByFluid().forEach { entry ->
            it[ForgeRegistries.FLUIDS.getDelegateOrThrow(entry.key)] = entry.value
        }
    }

    @JvmStatic
    fun getRenderLayers(state: BlockState): ChunkRenderTypeSet {
        if (state.block is LeavesBlock) {
            return if (ItemBlockRenderTypesAccessor.isRenderCutout())
                CUTOUT_MIPPED
            else SOLID
        }

        return BLOCK_RENDER_TYPES[ForgeRegistries.BLOCKS.getDelegateOrThrow(state.block)]!!
    }

    @JvmStatic
    fun setRenderLayer(block: Block, type: RenderType) {
        setRenderLayer(block, ChunkRenderTypeSet.of(type))
    }

    @JvmStatic
    @Synchronized
    fun setRenderLayer(block: Block, predicate: Predicate<RenderType>) {
        setRenderLayer(block, ChunkRenderTypeSet.of(RenderType.chunkBufferLayers().filter { predicate.test(it) }))
    }

    @JvmStatic
    @Synchronized
    fun setRenderLayer(block: Block, layers: ChunkRenderTypeSet) {
        checkClientLoading()
        ItemBlockRenderTypesAccessor.getTypeByBlock()[block] = layers.first()
        BLOCK_RENDER_TYPES[ForgeRegistries.BLOCKS.getDelegateOrThrow(block)] = layers
    }

    @JvmStatic
    @Synchronized
    fun setRenderLayer(fluid: Fluid, type: RenderType) {
        checkClientLoading()
        ItemBlockRenderTypesAccessor.getTypeByFluid()[fluid] = type
        FLUID_RENDER_TYPES[ForgeRegistries.FLUIDS.getDelegateOrThrow(fluid)] = type
    }

    private fun checkClientLoading() {
        Preconditions.checkState(KiltClient.hasInitialized, "Render layers can only be set during client loading!")
    }
}