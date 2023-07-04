package xyz.bluspring.kilt.workarounds

import com.mojang.datafixers.util.Pair
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import net.minecraftforge.client.model.geometry.IUnbakedGeometry
import java.util.function.Function
import io.github.fabricators_of_create.porting_lib.model.geometry.IUnbakedGeometry as FabricUnbakedGeometry

class UnbakedGeometryWrapper<T : FabricUnbakedGeometry<T>>(private val deferred: FabricUnbakedGeometry<T>) : IUnbakedGeometry<T> {
    override fun bake(
        context: IGeometryBakingContext?,
        bakery: ModelBakery?,
        spriteGetter: Function<Material, TextureAtlasSprite>?,
        modelState: ModelState?,
        overrides: ItemOverrides?,
        modelLocation: ResourceLocation?
    ): BakedModel {
        return deferred.bake(context, bakery, spriteGetter, modelState, overrides, modelLocation)
    }

    override fun getMaterials(
        context: IGeometryBakingContext?,
        modelGetter: Function<ResourceLocation, UnbakedModel>?,
        missingTextureErrors: MutableSet<Pair<String, String>>?
    ): MutableCollection<Material> {
        return deferred.getMaterials(context, modelGetter, missingTextureErrors)
    }
}