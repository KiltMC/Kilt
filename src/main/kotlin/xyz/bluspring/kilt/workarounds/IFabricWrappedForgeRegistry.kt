package xyz.bluspring.kilt.workarounds

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryInternal
import net.minecraftforge.registries.RegistryManager
import net.minecraftforge.registries.tags.ITagManager
import java.util.*

interface IFabricWrappedForgeRegistry<V> : Iterable<V>, IForgeRegistry<V> {
    val kiltRegistryKey: ResourceKey<Registry<V>>
    override fun getRegistryKey(): ResourceKey<Registry<V>> {
        return kiltRegistryKey
    }

    val kiltRegistryName: ResourceLocation

    override fun getRegistryName(): ResourceLocation {
        return kiltRegistryName
    }

    override fun register(key: String, value: V)
    override fun register(key: ResourceLocation, value: V)

    override fun containsKey(key: ResourceLocation): Boolean
    override fun containsValue(value: V): Boolean
    override fun isEmpty(): Boolean

    override fun getValue(key: ResourceLocation): V?
    override fun getKey(value: V): ResourceLocation?
    val kiltDefaultKey: ResourceLocation?
    override fun getDefaultKey(): ResourceLocation? {
        return kiltDefaultKey
    }

    override fun getResourceKey(value: V): Optional<ResourceKey<V>>

    val kiltKeys: Set<ResourceLocation>
    override fun getKeys(): Set<ResourceLocation> {
        return kiltKeys
    }

    val kiltValues: Collection<V>
    override fun getValues(): Collection<V> {
        return kiltValues
    }

    val kiltEntries: Set<Map.Entry<ResourceKey<V>, V>>
    override fun getEntries(): Set<Map.Entry<ResourceKey<V>, V>> {
        return kiltEntries
    }

    val kiltCodec: Codec<V>
    override fun getCodec(): Codec<V> {
        return kiltCodec
    }

    override fun getHolder(key: ResourceKey<V>): Optional<Holder<V>>
    override fun getHolder(location: ResourceLocation): Optional<Holder<V>>
    override fun getHolder(value: V): Optional<Holder<V>>

    override fun tags(): ITagManager<V>?

    override fun getDelegate(rkey: ResourceKey<V>): Optional<Holder.Reference<V>>
    override fun getDelegateOrThrow(rkey: ResourceKey<V>): Holder.Reference<V>
    override fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V>>
    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V>
    override fun getDelegate(value: V): Optional<Holder.Reference<V>>
    override fun getDelegateOrThrow(value: V): Holder.Reference<V>

    override fun <T> getSlaveMap(slaveMapName: ResourceLocation, type: Class<T>): T?

    fun interface AddCallback<V> : IForgeRegistry.AddCallback<V> {
        override fun onAdd(owner: IForgeRegistryInternal<V>, stage: RegistryManager, id: Int, key: ResourceKey<V>, obj: V, oldObj: V?)
    }

    fun interface ClearCallback<V> : IForgeRegistry.ClearCallback<V> {
        override fun onClear(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface CreateCallback<V> : IForgeRegistry.CreateCallback<V> {
        override fun onCreate(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface ValidateCallback<V> : IForgeRegistry.ValidateCallback<V> {
        override fun onValidate(owner: IForgeRegistryInternal<V>, stage: RegistryManager, id: Int, key: ResourceLocation, obj: V)
    }

    fun interface BakeCallback<V> : IForgeRegistry.BakeCallback<V> {
        override fun onBake(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface MissingFactory<V> : IForgeRegistry.MissingFactory<V> {
        override fun createMissing(key: ResourceLocation, isNetwork: Boolean): V
    }
}