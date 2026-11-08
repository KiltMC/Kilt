package xyz.bluspring.kilt.util.client

import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.ChunkRenderTypeSet
import net.neoforged.neoforge.client.extensions.IBakedModelExtension
import net.neoforged.neoforge.client.model.data.ModelData
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection
import xyz.bluspring.kilt.util.KiltHelper

object KiltClientHelper {
    @JvmStatic
    fun shouldFallbackToVanilla(modelData: ModelData?, state: BlockState, renderTypes: ChunkRenderTypeSet, model: BakedModel): Boolean {
        if ((modelData ?: ModelData.EMPTY) == ModelData.EMPTY && renderTypes.`kilt$size`() == 1 && ItemBlockRenderTypesInjection.getRenderLayers(state) == renderTypes) {
            if (renderTypes.first() != ItemBlockRenderTypes.getChunkRenderType(state))
                return false

            // Fun fact: NeoForge mods rely on this behaviour! Let's make sure this is limited to Neo mods only.
            if (state.block is LeavesBlock) {
                val key = state.block.builtInRegistryHolder().key
                if (key != null && Kilt.loader.hasMod(key.location().namespace))
                    return false
            }

            if (!KiltHelper.hasMethodOverride(model.javaClass, IBakedModelExtension::class.java, "getRenderTypes", BlockState::class.java, ModelData::class.java)) {
                return true
            }
        }

        return false
    }
}
