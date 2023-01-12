package net.minecraftforge.event

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.eventbus.api.GenericEvent

class AttachCapabilitiesEvent<T>(type: Class<T>, obj: T?) : GenericEvent<T>(type) {
    val `object` = obj

    val listeners = mutableListOf<Runnable>()
    val capabilities = mutableMapOf<ResourceLocation, ICapabilityProvider>()

    fun addCapability(key: ResourceLocation, cap: ICapabilityProvider) {
        if (capabilities.contains(key))
            throw IllegalStateException("Duplicate capability key: $key $cap")

        capabilities[key] = cap
    }

    fun addListener(listener: Runnable) {
        listeners.add(listener)
    }
}