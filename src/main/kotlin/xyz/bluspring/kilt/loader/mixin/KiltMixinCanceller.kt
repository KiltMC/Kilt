package xyz.bluspring.kilt.loader.mixin

import com.bawnorton.mixinsquared.api.MixinCanceller

class KiltMixinCanceller : MixinCanceller {
    private val cancelledMixins = listOf(
        "committee.nova.mkb.mixin.MixinKeyBinding",
        "committee.nova.mkb.mixin.MixinStickyKeyBinding"
    )

    override fun shouldCancel(targetClassNames: List<String>, mixinClassName: String): Boolean {
        return cancelledMixins.contains(mixinClassName)
    }
}