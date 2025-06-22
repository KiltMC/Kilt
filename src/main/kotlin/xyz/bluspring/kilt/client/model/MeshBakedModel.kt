package xyz.bluspring.kilt.client.model

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.RenderTypeGroup
import net.minecraftforge.client.extensions.IForgeBakedModel
import net.minecraftforge.client.model.data.ModelData
import java.util.function.Supplier

class MeshBakedModel(private val mesh: Mesh, private val hasAmbientOcclusion: Boolean, private val usesBlockLight: Boolean, private val isGui3d: Boolean, private val particleIcon: TextureAtlasSprite, private val transforms: ItemTransforms, private val overrides: ItemOverrides, private val renderTypes: RenderTypeGroup, private val renderTypesFast: RenderTypeGroup) : BakedModel, IForgeBakedModel {
    override fun getQuads(
        state: BlockState?,
        direction: Direction?,
        random: RandomSource
    ): List<BakedQuad?>? {
        return getQuads(state, direction, random, ModelData.EMPTY, null)
    }

    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        data: ModelData,
        renderType: RenderType?
    ): List<BakedQuad?> {
        return listOf()//super.getQuads(state, side, rand, data, renderType)
    }

    override fun emitBlockQuads(
        blockView: BlockAndTintGetter,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<RandomSource?>,
        context: RenderContext
    ) {
        mesh.outputTo(context.emitter)
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource?>, context: RenderContext) {
        mesh.outputTo(context.emitter)
    }

    override fun isVanillaAdapter(): Boolean {
        return false
    }

    override fun useAmbientOcclusion(): Boolean {
        return hasAmbientOcclusion
    }

    override fun isGui3d(): Boolean {
        return isGui3d
    }

    override fun usesBlockLight(): Boolean {
        return usesBlockLight
    }

    override fun isCustomRenderer(): Boolean {
        return false
    }

    override fun getParticleIcon(): TextureAtlasSprite? {
        return particleIcon
    }

    override fun getTransforms(): ItemTransforms? {
        return transforms
    }

    override fun getOverrides(): ItemOverrides? {
        return overrides
    }
}