package xyz.bluspring.kilt.workarounds

import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import net.minecraftforge.client.model.geometry.IUnbakedGeometry
import java.util.function.Function
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry as FabricUnbakedGeometry

class UnbakedGeometryWrapper<T : IUnbakedGeometry<T>>(private val deferred: FabricUnbakedGeometry<T>) : IUnbakedGeometry<T> {
    override fun bake(
        context: IGeometryBakingContext,
        baker: ModelBaker?,
        spriteGetter: Function<Material, TextureAtlasSprite>?,
        modelState: ModelState?,
        overrides: ItemOverrides?,
        modelLocation: ResourceLocation
    ): BakedModel? {
        return if (context is BlockGeometryBakingContext) {
            deferred.bake(context.owner, baker, spriteGetter, modelState, overrides, modelLocation, context.isGui3d)
        } else null
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

    override fun resolveParents(modelGetter: Function<ResourceLocation, UnbakedModel>?, context: BlockModel?) {
        deferred.resolveParents(modelGetter, context)
    }
}