package xyz.bluspring.kilt.loader.asm

import org.spongepowered.asm.mixin.extensibility.IMixinConfig

object KiltInstrumentationHelper {
    @Suppress("unused")
    @JvmStatic
    fun checkShouldConformOverwriteVisibility(mixinConfig: IMixinConfig): Boolean {
        return true
    }
}