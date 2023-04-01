package net.minecraftforge.registries

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraftforge.registries.tags.IReverseTag
import net.minecraftforge.registries.tags.ITag
import net.minecraftforge.registries.tags.ITagManager
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.streams.toList

class ForgeRegistryTagManager<V : Any> internal constructor(
    private val forgeRegistry: ForgeRegistry<V>
) : ITagManager<V> {
    private val tags = mutableMapOf<TagKey<V>, ITag<V>>()

    override fun getTag(name: TagKey<V>): ITag<V> {
        val vanillaTag = forgeRegistry.vanillaRegistry.getOrCreateTag(name)

        return tags.computeIfAbsent(name) {
            ForgeRegistryTag(name, vanillaTag)
        }
    }

    override fun getReverseTag(value: V): Optional<IReverseTag<V>> {
        // An interface is supposed to get injected into here,
        // I'm not sure how well it works though. So ignore the
        // unchecked cast warning, nothing much we can do about it.
        return Optional.ofNullable(Holder.direct(value) as IReverseTag<V>)
    }

    override fun isKnownTagName(name: TagKey<V>): Boolean {
        return forgeRegistry.vanillaRegistry.isKnownTagName(name)
    }

    override fun stream(): Stream<ITag<V>> {
        // I fear for the memory usage.
        return tags.values.stream()
    }

    override fun getTagNames(): Stream<TagKey<V>> {
        return forgeRegistry.vanillaRegistry.tagNames
    }

    override fun createTagKey(location: ResourceLocation): TagKey<V> {
        return TagKey.create(forgeRegistry.registryKey, location)
    }

    override fun addOptionalTagDefaults(name: TagKey<V>, defaults: Set<Supplier<V>>) {
        forgeRegistry.holderHelper.ifPresent {
            it.addOptionalTag(name, defaults)
        }
    }

    override fun createOptionalTagKey(location: ResourceLocation, defaults: Set<Supplier<V>>): TagKey<V> {
        return createTagKey(location).apply {
            addOptionalTagDefaults(this, defaults)
        }
    }

    override fun iterator(): MutableIterator<ITag<V>> {
        return tags.values.iterator()
    }

    // why is defaultedTags unused???
    internal fun bind(holderTags: Map<TagKey<V>, HolderSet.Named<V>>, defaultedTags: Set<TagKey<V>>) {
        holderTags.forEach { (key, contents) ->
            (tags.computeIfAbsent(key, ::ForgeRegistryTag) as ForgeRegistryTag<V>).bind(contents)
        }
    }
}