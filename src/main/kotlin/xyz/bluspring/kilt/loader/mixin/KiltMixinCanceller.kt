package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.api.MixinCanceller

class KiltMixinCanceller : MixinCanceller {
    private val cancelledMixins = listOf(
        "de.florianmichael.asmfabricloader.hook.mixin.MixinTitleScreen",
        "com.anthonyhilyard.iceberg.mixin.MinecraftMixin"
    )

    override fun shouldCancel(targetClasses: List<String>, mixinClassName: String): Boolean {
        return cancelledMixins.contains(mixinClassName)
    }
}