package net.minecraftforge.registries.tags

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import java.util.Optional
import java.util.function.Supplier
import java.util.stream.Stream

interface ITagManager<V> : Iterable<ITag<V>> {
    fun getTag(name: TagKey<V>): ITag<V>
    fun getReverseTag(value: V): Optional<IReverseTag<V>>
    fun isKnownTagName(name: TagKey<V>): Boolean

    fun stream(): Stream<ITag<V>>

    val tagNames: Stream<TagKey<V>>

    fun createTagKey(location: ResourceLocation): TagKey<V>
    fun createOptionalTagKey(location: ResourceLocation, defaults: Set<Supplier<V>>): TagKey<V>

    fun addOptionalTagDefaults(name: TagKey<V>, defaults: Set<Supplier<V>>)
}