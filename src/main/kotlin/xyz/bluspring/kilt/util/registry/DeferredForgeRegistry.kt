package xyz.bluspring.kilt.util.registry

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegistryManager
import net.minecraftforge.registries.tags.ITagManager
import java.util.*

class DeferredForgeRegistry<V>(private val key: ResourceLocation) : IForgeRegistry<V> {
    private val wrapped: IForgeRegistry<V>?
        get() {
            return RegistryManager.ACTIVE.getRegistry(key)
        }

    override fun getRegistryKey(): ResourceKey<Registry<V>> {
        return ResourceKey.createRegistryKey(this.key)
    }

    override fun getRegistryName(): ResourceLocation {
        return this.key
    }

    override fun register(key: String?, value: V?) {
        this.wrapped?.register(key, value)
    }

    override fun register(key: ResourceLocation?, value: V?) {
        this.wrapped?.register(key, value)
    }

    override fun containsKey(key: ResourceLocation?): Boolean {
        return this.wrapped?.containsKey(key) ?: false
    }

    override fun containsValue(value: V?): Boolean {
        return this.wrapped?.containsValue(value) ?: false
    }

    override fun isEmpty(): Boolean {
        return this.wrapped?.isEmpty ?: true
    }

    override fun getValue(key: ResourceLocation?): V? {
        return this.wrapped?.getValue(key)
    }

    override fun getKey(value: V?): ResourceLocation? {
        return this.wrapped?.getKey(value)
    }

    override fun getDefaultKey(): ResourceLocation? {
        return this.wrapped?.defaultKey
    }

    override fun getResourceKey(value: V?): Optional<ResourceKey<V>> {
        return this.wrapped?.getResourceKey(value) ?: Optional.empty()
    }

    override fun getKeys(): Set<ResourceLocation> {
        return this.wrapped?.keys ?: emptySet()
    }

    override fun getValues(): Collection<V> {
        return this.wrapped?.values ?: emptyList()
    }

    override fun getEntries(): Set<Map.Entry<ResourceKey<V>, V>> {
        return this.wrapped?.entries ?: emptySet()
    }

    override fun getCodec(): Codec<V?> {
        return this.wrapped?.codec ?: Codec.unit(null)
    }

    override fun getHolder(key: ResourceKey<V>): Optional<Holder<V>> {
        return this.wrapped?.getHolder(key) ?: Optional.empty()
    }

    override fun getHolder(location: ResourceLocation?): Optional<Holder<V>> {
        return this.wrapped?.getHolder(location) ?: Optional.empty()
    }

    override fun getHolder(value: V?): Optional<Holder<V>> {
        return this.wrapped?.getHolder(value) ?: Optional.empty()
    }

    override fun tags(): ITagManager<V?>? {
        return this.wrapped?.tags()
    }

    override fun getDelegate(rkey: ResourceKey<V>): Optional<Holder.Reference<V>> {
        return this.wrapped?.getDelegate(rkey) ?: Optional.empty()
    }

    override fun getDelegateOrThrow(rkey: ResourceKey<V>): Holder.Reference<V> {
        return this.wrapped!!.getDelegateOrThrow(rkey)
    }

    override fun getDelegate(key: ResourceLocation?): Optional<Holder.Reference<V>> {
        return this.wrapped?.getDelegate(key) ?: Optional.empty()
    }

    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V> {
        return this.wrapped!!.getDelegateOrThrow(key)
    }

    override fun getDelegate(value: V?): Optional<Holder.Reference<V>> {
        return this.wrapped?.getDelegate(value) ?: Optional.empty()
    }

    override fun getDelegateOrThrow(value: V?): Holder.Reference<V?> {
        return this.wrapped!!.getDelegateOrThrow(value)
    }

    override fun <T : Any?> getSlaveMap(
        slaveMapName: ResourceLocation?,
        type: Class<T?>?
    ): T? {
        return this.wrapped?.getSlaveMap(slaveMapName, type)
    }

    override fun iterator(): MutableIterator<V> {
        return this.wrapped?.iterator() ?: mutableListOf<V>().iterator()
    }
}