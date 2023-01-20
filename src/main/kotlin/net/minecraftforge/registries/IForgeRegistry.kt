package net.minecraftforge.registries

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.tags.ITagManager
import java.util.Optional

interface IForgeRegistry<V> : Iterable<V> {
    val registryKey: ResourceKey<Registry<V>>
    val registryName: ResourceLocation

    fun register(key: String, value: V)
    fun register(key: ResourceLocation, value: V)

    fun containsKey(key: ResourceLocation): Boolean
    fun containsValue(value: V): Boolean
    fun isEmpty(): Boolean

    fun getValue(key: ResourceLocation): V?
    fun getKey(value: V): ResourceLocation?
    val defaultKey: ResourceLocation?
    fun getResourceKey(value: V): Optional<ResourceKey<V>>

    val keys: Set<ResourceLocation>
    val values: Collection<V>
    val entries: Set<Map.Entry<ResourceKey<V>, V>>

    val codec: Codec<V>

    fun getHolder(key: ResourceKey<V>): Optional<Holder<V>>
    fun getHolder(location: ResourceLocation): Optional<Holder<V>>
    fun getHolder(value: V): Optional<Holder<V>>

    fun tags(): ITagManager<V>?

    fun getDelegate(rkey: ResourceKey<V>): Optional<Holder.Reference<V>>
    fun getDelegateOrThrow(rkey: ResourceKey<V>): Holder.Reference<V>
    fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V>>
    fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V>
    fun getDelegate(value: V): Optional<Holder.Reference<V>>
    fun getDelegateOrThrow(value: V): Holder.Reference<V>

    fun <T> getSlaveMap(slaveMapName: ResourceLocation, type: Class<T>): T
    fun setSlaveMap(name: ResourceLocation, obj: Any)

    fun interface AddCallback<V> {
        fun onAdd(owner: IForgeRegistryInternal<V>, stage: RegistryManager, id: Int, key: ResourceKey<V>, obj: V, oldObj: V?)
    }

    fun interface ClearCallback<V> {
        fun onClear(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface CreateCallback<V> {
        fun onCreate(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface ValidateCallback<V> {
        fun onValidate(owner: IForgeRegistryInternal<V>, stage: RegistryManager, id: Int, key: ResourceLocation, obj: V)
    }

    fun interface BakeCallback<V> {
        fun onBake(owner: IForgeRegistryInternal<V>, stage: RegistryManager)
    }

    fun interface DummyFactory<V> {
        fun createDummy(key: ResourceLocation): V
    }

    fun interface MissingFactory<V> {
        fun createMissing(key: ResourceLocation, isNetwork: Boolean): V
    }
}