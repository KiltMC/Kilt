package net.minecraftforge.registries

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.network.HandshakeMessages
import xyz.bluspring.kilt.Kilt

class RegistryManager(val name: String) {
    @JvmField val registries = mutableMapOf<ResourceLocation, ForgeRegistry<*>>()
    internal constructor() : this("STAGING")

    fun <V> getRegistry(key: ResourceLocation): ForgeRegistry<V>? {
        return registries[key] as ForgeRegistry<V>?
    }

    fun <V> getRegistry(key: ResourceKey<out Registry<V>>): ForgeRegistry<V>? {
        return getRegistry(key.location())
    }

    fun <V> getRegistry(key: ResourceLocation, other: RegistryManager): ForgeRegistry<V>? {
        if (!registries.contains(key)) {
            return other.getRegistry<V>(key).apply {
                if (this != null)
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
        val registry = ForgeRegistry<V>(this, name, builder)
        registries[name] = registry

        return registry
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

            ModLoader.get().kiltPostEventWrappingMods(event)
        }

        @JvmStatic
        val vanillaRegistryKeys: Set<ResourceLocation>
            get() = Registry.REGISTRY.keySet()

        @JvmStatic
        val registryNamesForSyncToClient: List<ResourceLocation>
            get() = ACTIVE.registries.keys.toList()

        @JvmStatic
        fun generateRegistryPackets(isLocal: Boolean): List<org.apache.commons.lang3.tuple.Pair<String, HandshakeMessages.S2CRegistry>> {
            return if (!isLocal) {
                ACTIVE.registries.map {
                    org.apache.commons.lang3.tuple.Pair.of("Registry ${it.key}", HandshakeMessages.S2CRegistry(it.key, ForgeRegistry.Snapshot().apply {
                        it.value.vanillaRegistry.keySet().forEach { key ->
                            this.ids[key] = it.value.getID(key)
                        }
                    }))
                }
            } else listOf()
        }
    }
}