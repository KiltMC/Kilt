package net.minecraftforge.registries

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraftforge.registries.tags.IReverseTag
import net.minecraftforge.registries.tags.ITag
import net.minecraftforge.registries.tags.ITagManager
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream

class ForgeRegistryTagManager<V> internal constructor(
    private val forgeRegistry: ForgeRegistry<V>
) : ITagManager<V> {
    override fun getTag(name: TagKey<V>): ITag<V> {
        val vanillaTag = forgeRegistry.vanillaRegistry.getOrCreateTag(name)

        // this is terrible, but this is probably the
        // safest way I can do this.
        return ForgeRegistryTag(name, vanillaTag)
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
        return forgeRegistry.vanillaRegistry.tags.map { ForgeRegistryTag<V>(it.first, it.second) }
    }

    override val tagNames: Stream<TagKey<V>>
        get() = forgeRegistry.vanillaRegistry.tagNames

    override fun createTagKey(location: ResourceLocation): TagKey<V> {
        return TagKey.create(forgeRegistry.registryKey, location)
    }

    override fun addOptionalTagDefaults(name: TagKey<V>, defaults: Set<Supplier<V>>) {
        TODO("Not yet implemented")
    }

    override fun createOptionalTagKey(location: ResourceLocation, defaults: Set<Supplier<V>>): TagKey<V> {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<ITag<V>> {
        return forgeRegistry.vanillaRegistry.tags.map { ForgeRegistryTag<V>(it.first, it.second) }.iterator()
    }

}