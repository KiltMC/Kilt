package xyz.bluspring.kilt.workarounds

import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import net.minecraftforge.client.model.geometry.IUnbakedGeometry
import java.util.function.Function
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry as FabricUnbakedGeometry

class ForgeWrappedFabricUnbakedGeometry<T : FabricUnbakedGeometry<T>, U : IUnbakedGeometry<U>>(val deferred: FabricUnbakedGeometry<T>) : IUnbakedGeometry<U> {
    override fun bake(
        context: IGeometryBakingContext,
        baker: ModelBaker?,
        spriteGetter: Function<Material, TextureAtlasSprite>?,
        modelState: ModelState?,
        overrides: ItemOverrides?,
        modelLocation: ResourceLocation?
    ): BakedModel {
        if (deferred is IUnbakedGeometry<*>) {
            return deferred.bake(context, baker, spriteGetter, modelState, overrides, modelLocation)
        }

        if (context is BlockGeometryBakingContext)
            return deferred.bake(context.owner, baker, spriteGetter, modelState, overrides, modelLocation, context.isGui3d)


        return deferred.bake(EMPTY_BLOCK_MODEL, baker, spriteGetter, modelState, overrides, modelLocation, context.isGui3d)
    }

    override fun bake(
        context: BlockModel?,
        baker: ModelBaker?,
        spriteGetter: Function<Material, TextureAtlasSprite>?,
        modelState: ModelState?,
        overrides: ItemOverrides?,
        modelLocation: ResourceLocation?,
        isGui3d: Boolean
    ): BakedModel {
        return deferred.bake(context, baker, spriteGetter, modelState, overrides, modelLocation, isGui3d)
    }

    companion object {
        val EMPTY_BLOCK_MODEL = BlockModel(ResourceLocation("empty"), listOf(), mapOf(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, listOf())
    }
}