package xyz.bluspring.kilt.workarounds

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.capabilities.CapabilityDispatcher
import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl
import java.util.function.Supplier

// Due to a mixin issue where you can't extend new classes in it, this is a
// little workaround to help redirect method calls to CapabilityProvider.
class CapabilityProviderWorkaround<B : ICapabilityProviderImpl<B>>(val baseClass: Class<B>, isLazy: Boolean, val base: B) : CapabilityProvider<B>(baseClass, isLazy) {
    constructor(baseClass: Class<B>, base: B) : this(baseClass, false, base)

    fun invokeGatherCapabilities() {
        this.gatherCapabilities()
    }

    fun invokeGatherCapabilities(parent: ICapabilityProvider?) {
        this.gatherCapabilities(parent)
    }

    fun invokeGatherCapabilities(parent: Supplier<ICapabilityProvider>?) {
        this.gatherCapabilities(parent)
    }

    fun invokeGetCapabilities(): CapabilityDispatcher? {
        return this.capabilities
    }

    fun invokeSerializeCaps(): CompoundTag? {
        return this.serializeCaps()
    }

    fun invokeDeserializeCaps(tag: CompoundTag) {
        this.deserializeCaps(tag)
    }

    override fun getProvider(): B {
        return base
    }
}