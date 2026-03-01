package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.api.MixinCanceller
import xyz.bluspring.kilt.Kilt

class KiltMixinCanceller : MixinCanceller {
    private val cancelledMixins = listOf(
        "committee.nova.mkb.mixin.MixinKeyBinding",
        "committee.nova.mkb.mixin.MixinStickyKeyBinding",
        "org.violetmoon.quark.mixin.mixins.client.accessor.AccessorCustomCreativeSlot",
        "org.violetmoon.quark.mixin.mixins.ConcretePowderBlockMixin",
        "org.violetmoon.quark.mixin.mixins.client.LevelRendererMixin",
        "dev.ghen.thirst.foundation.mixin.MixinPotionItem",
        "com.lowdragmc.lowdraglib.forge.core.mixins.BlockRenderDispatcherMixin",
        "fuzs.nightconfigfixes.mixin.ConfigParserFabricMixin", // Replaced by ConfigParserTransform in
        "com.gregtechceu.gtceu.core.mixins.client.MultiPlayerGameModeMixin" //TODO: REMOVE THIS AND ACTUALLY FIX THE PROBLEM
    )

    override fun shouldCancel(targetClassNames: List<String>, mixinClassName: String): Boolean {
        // special case for Create
        if (Kilt.loader.hasMod("create") && mixinClassName == "com.simibubi.create.foundation.mixin.client.MapRendererMapInstanceMixin") {
            return true
        }

        return cancelledMixins.contains(mixinClassName)
    }
}