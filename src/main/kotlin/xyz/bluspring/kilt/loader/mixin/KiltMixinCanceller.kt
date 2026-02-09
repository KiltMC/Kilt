package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.api.MixinCanceller

class KiltMixinCanceller : MixinCanceller {
    private val cancelledMixins = listOf(
        "committee.nova.mkb.mixin.MixinKeyBinding",
        "committee.nova.mkb.mixin.MixinStickyKeyBinding",
        "org.violetmoon.quark.mixin.mixins.client.accessor.AccessorCustomCreativeSlot",
        "org.violetmoon.quark.mixin.mixins.ConcretePowderBlockMixin",
        "org.violetmoon.quark.mixin.mixins.client.LevelRendererMixin",
        "dev.ghen.thirst.foundation.mixin.MixinPotionItem",
        "com.lowdragmc.lowdraglib.forge.core.mixins.BlockRenderDispatcherMixin",
        "fuzs.nightconfigfixes.mixin.ConfigParserFabricMixin", // Replaced by ConfigParserTransform in Kilt
        "io.github.fabricators_of_create.porting_lib.resources.mixin.common.SimplePreparableReloadListenerMixin", // We handle this ourselves.
    )

    override fun shouldCancel(targetClassNames: List<String>, mixinClassName: String): Boolean {
        return cancelledMixins.contains(mixinClassName)
    }
}