package xyz.bluspring.kilt.workarounds

import com.mojang.math.Transformation
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.resources.model.Material
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryBakingContext as FabricGeometryBakingContext

class GeometryBakingContextWrapper(private val deferred: FabricGeometryBakingContext) : IGeometryBakingContext {
    override fun getModelName(): String {
        return deferred.modelName
    }

    override fun hasMaterial(name: String?): Boolean {
        return deferred.hasMaterial(name)
    }

    override fun getMaterial(name: String?): Material {
        return deferred.getMaterial(name)
    }

    override fun isGui3d(): Boolean {
        return deferred.isGui3d
    }

    override fun useBlockLight(): Boolean {
        return deferred.useBlockLight()
    }

    override fun useAmbientOcclusion(): Boolean {
        return deferred.useAmbientOcclusion()
    }

    override fun getTransforms(): ItemTransforms {
        return deferred.transforms
    }

    override fun getRootTransform(): Transformation {
        return deferred.rootTransform
    }

    override fun getRenderTypeHint(): ResourceLocation? {
        return deferred.renderTypeHint
    }

    override fun isComponentVisible(component: String?, fallback: Boolean): Boolean {
        return deferred.isComponentVisible(component, fallback)
    }
}