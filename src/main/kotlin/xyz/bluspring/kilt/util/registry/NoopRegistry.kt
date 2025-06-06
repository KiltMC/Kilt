package xyz.bluspring.kilt.util.registry

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Lifecycle
import net.minecraft.core.*
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import java.util.*
import java.util.stream.Stream

class NoopRegistry<V : Any>(private val key: ResourceKey<out Registry<V>>) : Registry<V> {
    override fun key(): ResourceKey<out Registry<V>> {
        return key
    }

    override fun getKey(value: V): ResourceLocation? {
        return null
    }

    override fun getResourceKey(value: V): Optional<ResourceKey<V>> {
        return Optional.empty()
    }

    override fun getId(value: V?): Int {
        return -1
    }

    override fun get(key: ResourceKey<V?>?): V? {
        return null
    }

    override fun get(name: ResourceLocation?): V? {
        return null
    }

    override fun lifecycle(value: V): Lifecycle? {
        return Lifecycle.stable()
    }

    override fun registryLifecycle(): Lifecycle? {
        return Lifecycle.stable()
    }

    override fun keySet(): Set<ResourceLocation> {
        return emptySet()
    }

    override fun entrySet(): Set<Map.Entry<ResourceKey<V?>?, V?>?>? {
        return emptySet()
    }

    override fun registryKeySet(): Set<ResourceKey<V?>?>? {
        return emptySet()
    }

    override fun getRandom(random: RandomSource): Optional<Holder.Reference<V>> {
        return Optional.empty()
    }

    override fun containsKey(name: ResourceLocation): Boolean {
        return false
    }

    override fun containsKey(key: ResourceKey<V?>): Boolean {
        return false
    }

    override fun freeze(): Registry<V> {
        return this
    }

    override fun createIntrusiveHolder(value: V): Holder.Reference<V?>? {
        return null
    }

    override fun getHolder(id: Int): Optional<Holder.Reference<V>> {
        return Optional.empty()
    }

    override fun getHolder(key: ResourceKey<V?>): Optional<Holder.Reference<V>> {
        return Optional.empty()
    }

    override fun wrapAsHolder(value: V): Holder<V?>? {
        return null
    }

    override fun holders(): Stream<Holder.Reference<V?>?>? {
        return Stream.empty()
    }

    override fun getTag(key: TagKey<V?>): Optional<HolderSet.Named<V>> {
        return Optional.empty()
    }

    override fun getOrCreateTag(key: TagKey<V?>): HolderSet.Named<V?>? {
        return null
    }

    override fun getTags(): Stream<Pair<TagKey<V?>?, HolderSet.Named<V?>?>?>? {
        return Stream.empty()
    }

    override fun getTagNames(): Stream<TagKey<V?>?>? {
        return Stream.empty()
    }

    override fun resetTags() {
    }

    override fun bindTags(tagMap: Map<TagKey<V?>?, List<Holder<V?>?>?>) {
    }

    override fun holderOwner(): HolderOwner<V?>? {
        return null
    }

    override fun asLookup(): HolderLookup.RegistryLookup<V>? {
        return null
    }

    override fun byId(id: Int): V? {
        return null
    }

    override fun size(): Int {
        return 0
    }

    override fun iterator(): MutableIterator<V> {
        return mutableListOf<V>().iterator()
    }
}