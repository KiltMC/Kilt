package net.minecraftforge.registries

import com.mojang.serialization.Codec
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.tags.ITagManager
import xyz.bluspring.kilt.Kilt
import java.util.*

class ForgeRegistry<V> internal constructor (
    override val registryName: ResourceLocation,
    private val builder: RegistryBuilder<V>
) : IForgeRegistry<V> {
    override val registryKey: ResourceKey<Registry<V>> = ResourceKey.createRegistryKey(registryName)
    private val fabricRegistry = LazyRegistrar.create<V>(registryName, Kilt.MOD_ID)
    private val vanillaRegistryGetter = fabricRegistry.makeRegistry()
    val vanillaRegistry: Registry<V>
        get() {
            return vanillaRegistryGetter.get()
        }

    private val tagManager = ForgeRegistryTagManager(this)

    override val keys: Set<ResourceLocation>
        get() {
            return vanillaRegistry.keySet()
        }
    override val values: Collection<V>
        get() {
            return vanillaRegistry.entrySet().map { it.value }
        }
    override val entries: Set<Map.Entry<ResourceKey<V>, V>>
        get() {
            return vanillaRegistry.entrySet()
        }
    override val codec: Codec<V>
        get() {
            return vanillaRegistry.byNameCodec()
        }

    override fun containsKey(key: ResourceLocation): Boolean {
        return vanillaRegistry.containsKey(key)
    }

    override fun isEmpty(): Boolean {
        return vanillaRegistry.keySet().isEmpty()
    }

    override fun getValue(key: ResourceLocation): V? {
        return vanillaRegistry.get(key)
    }

    // what the fuck is this even used for
    override fun getDefaultKey(): ResourceLocation? {
        return keys.firstOrNull()
    }

    override fun getResourceKey(value: V): Optional<ResourceKey<V>> {
        return vanillaRegistry.getResourceKey(value)
    }

    override fun getKey(value: V): ResourceLocation? {
        return vanillaRegistry.getKey(value)
    }

    override fun containsValue(value: V): Boolean {
        return values.any { it == value }
    }

    override fun register(key: ResourceLocation, value: V) {
        fabricRegistry.register(key) { value }
    }

    override fun register(key: String, value: V) {
        fabricRegistry.register(key) { value }
    }

    override fun getHolder(location: ResourceLocation): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(ResourceKey.create(registryKey, location))
    }

    override fun tags(): ITagManager<V> {
        return tagManager
    }

    override fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.key().location() == key })
    }

    override fun getDelegate(value: V): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.value() == value })
    }

    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.key().location() == key }
    }

    override fun <T> getSlaveMap(slaveMapName: ResourceLocation, type: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun getDelegateOrThrow(value: V): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.value() == value }
    }

    override fun getDelegateOrThrow(rkey: ResourceKey<V>): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.key() == rkey }
    }

    override fun getDelegate(rkey: ResourceKey<V>): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.key() == rkey })
    }

    override fun getHolder(value: V): Optional<Holder<V>> {
        val key = vanillaRegistry.holders().toList().firstOrNull { it.value() == value }?.key() ?: return Optional.empty()

        return vanillaRegistry.getHolder(key)
    }

    override fun getHolder(key: ResourceKey<V>): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(key)
    }

    override fun iterator(): Iterator<V> {
        return vanillaRegistry.iterator()
    }
}