package xyz.bluspring.kilt.workarounds

import net.minecraft.util.context.ContextKey
import net.neoforged.neoforge.client.extensions.IRenderStateExtension
import org.jetbrains.annotations.ApiStatus

interface BaseRenderStateWorkaround : IRenderStateExtension {
    @ApiStatus.Internal
    fun resetRenderData()

    fun `kilt$getExtensions`(): Map<ContextKey<*>, Any>
}
