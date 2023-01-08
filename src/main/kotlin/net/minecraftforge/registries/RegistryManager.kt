package net.minecraftforge.registries

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.kilt.Kilt

class RegistryManager(val name: String) {
    private val registries = mutableMapOf<ResourceLocation, ForgeRegistry<*>>()
    internal constructor() : this("STAGING")

    fun <V> getRegistry(key: ResourceLocation): ForgeRegistry<V> {
        return if (registries.contains(key))
            registries[key] as ForgeRegistry<V>
        else {
            val registry = ForgeRegistry<V>(key, RegistryBuilder())
            registries[key] = registry

            registry
        }
    }

    fun <V> getRegistry(key: ResourceKey<out Registry<V>>): ForgeRegistry<V> {
        return getRegistry(key.location())
    }

    fun <V> getName(reg: IForgeRegistry<V>): ResourceLocation? {
        return registries.entries.firstOrNull { it.value == reg }?.key
    }

    companion object {
        @JvmStatic
        fun postNewRegistryEvent() {
            val event = NewRegistryEvent()

            Kilt.loader.postEvent(event)
        }
    }
}