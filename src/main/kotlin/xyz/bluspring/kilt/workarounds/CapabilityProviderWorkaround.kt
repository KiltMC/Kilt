package xyz.bluspring.kilt.workarounds

import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl

// Deferred capability provider for anything that's extended via mixin for Kilt.
class CapabilityProviderWorkaround<B : ICapabilityProviderImpl<B>>(val baseClass: Class<B>, isLazy: Boolean, val base: B) : CapabilityProvider<B>(baseClass, isLazy) {
    override fun getProvider(): B {
        return base
    }
}