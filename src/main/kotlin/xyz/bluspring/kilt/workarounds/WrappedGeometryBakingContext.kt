package xyz.bluspring.kilt.workarounds

import com.google.common.cache.CacheBuilder
import com.mojang.math.Transformation
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.resources.model.Material
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryBakingContext as PortingLibGeometryBakingContext

class WrappedGeometryBakingContext private constructor(val wrapped: PortingLibGeometryBakingContext) : IGeometryBakingContext {
    override fun getModelName(): String? = wrapped.modelName
    override fun hasMaterial(name: String): Boolean = wrapped.hasMaterial(name)
    override fun getMaterial(name: String): Material? = wrapped.getMaterial(name)
    override fun isGui3d(): Boolean = wrapped.isGui3d
    override fun useBlockLight(): Boolean = wrapped.useBlockLight()
    override fun useAmbientOcclusion(): Boolean = wrapped.useAmbientOcclusion()
    override fun getTransforms(): ItemTransforms? = wrapped.transforms
    override fun getRootTransform(): Transformation? = wrapped.rootTransform
    override fun getRenderTypeHint(): ResourceLocation? = wrapped.renderTypeHint
    override fun isComponentVisible(component: String, fallback: Boolean): Boolean = wrapped.isComponentVisible(component, fallback)

    companion object {
        private val contexts = CacheBuilder.newBuilder()
            .expireAfterAccess(5.minutes.toJavaDuration())
            .maximumSize(100)
            .build<PortingLibGeometryBakingContext, WrappedGeometryBakingContext>()

        @JvmStatic
        fun wrap(wrapped: PortingLibGeometryBakingContext): IGeometryBakingContext {
            return contexts.get(wrapped) { WrappedGeometryBakingContext(wrapped) }
        }
    }
}