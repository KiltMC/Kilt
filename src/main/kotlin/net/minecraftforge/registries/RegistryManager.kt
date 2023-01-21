package net.minecraftforge.registries

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.kilt.Kilt

class RegistryManager(val name: String) {
    @JvmField val registries = mutableMapOf<ResourceLocation, ForgeRegistry<*>>()
    internal constructor() : this("STAGING")

    fun <V> getRegistry(key: ResourceLocation): ForgeRegistry<V> {
        return if (registries.contains(key))
            registries[key] as ForgeRegistry<V>
        else {
            val registry = ForgeRegistry<V>(this, key, RegistryBuilder())
            registries[key] = registry

            registry
        }
    }

    fun <V> getRegistry(key: ResourceKey<out Registry<V>>): ForgeRegistry<V> {
        return getRegistry(key.location())
    }

    fun <V> getRegistry(key: ResourceLocation, other: RegistryManager): ForgeRegistry<V> {
        if (!registries.contains(key)) {
            return other.getRegistry<V>(key).apply {
                registries[key] = this
            }
        }

        return getRegistry(key)
    }

    fun <V> getName(reg: IForgeRegistry<V>): ResourceLocation? {
        return registries.entries.firstOrNull { it.value == reg }?.key
    }

    @JvmName("createRegistry")
    internal fun <V> createRegistry(name: ResourceLocation, builder: RegistryBuilder<V>): ForgeRegistry<V> {
        return getRegistry(name) // literally do not care
    }

    companion object {
        @JvmField
        val ACTIVE = RegistryManager("ACTIVE")

        @JvmField
        val VANILLA = RegistryManager("VANILLA")

        @JvmField
        val FROZEN = RegistryManager("FROZEN")

        @JvmStatic
        fun postNewRegistryEvent() {
            val event = NewRegistryEvent()

            Kilt.loader.postEvent(event)
        }

        @JvmStatic
        val vanillaRegistryKeys: Set<ResourceLocation>
            get() = Registry.REGISTRY.keySet()

        @JvmStatic
        val registryNamesForSyncToClient: List<ResourceLocation>
            get() = ACTIVE.registries.keys.toList()
    }
}