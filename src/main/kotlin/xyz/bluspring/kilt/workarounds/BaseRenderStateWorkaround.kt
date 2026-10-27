package xyz.bluspring.kilt.workarounds

import net.minecraft.util.context.ContextKey
import net.neoforged.neoforge.client.extensions.IRenderStateExtension
import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.kilt.util.KiltHelper

interface BaseRenderStateWorkaround : IRenderStateExtension {
    @ApiStatus.Internal
    fun resetRenderData() {
        throw KiltHelper.createMixinException(this::class.java, "resetRenderData")
    }

    fun `kilt$getExtensions`(): Map<ContextKey<*>, Any> {
        throw KiltHelper.createMixinException(this::class.java, $$"kilt$getExtensions")
    }
}
