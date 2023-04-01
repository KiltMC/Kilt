package net.minecraftforge.registries

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraftforge.registries.tags.ITag
import java.util.*
import java.util.stream.Stream

class ForgeRegistryTag<V : Any> internal constructor(
    private val key: TagKey<V>
) : ITag<V> {
    private var contents: HolderSet.Named<V>? = null

    internal constructor(key: TagKey<V>, contents: HolderSet.Named<V>?) : this(key) {
        this.contents = contents
    }

    override fun stream(): Stream<V> {
        return if (contents == null)
            listOf<V>().stream()
        else
            contents!!.map { it.value() }.stream()
    }

    override fun isEmpty(): Boolean {
        return if (contents == null) true else contents!!.size() == 0
    }

    override fun size(): Int {
        return if (contents == null) 0 else contents!!.size()
    }

    override fun getRandomElement(random: RandomSource): Optional<V> {
        val randomElement = contents?.getRandomElement(random)

        val value = if (randomElement?.isPresent == true)
            randomElement.get().value()
        else null

        return Optional.ofNullable(value)
    }

    override fun isBound(): Boolean {
        return contents != null
    }

    override fun contains(value: V): Boolean {
        return contents?.contains(Holder.direct(value)) == true
    }

    override fun iterator(): MutableIterator<V> {
        return if (contents == null)
            mutableListOf<V>().iterator()
        else
            contents!!.map { it.value() }.toMutableList().iterator()
    }

    override fun getKey(): TagKey<V> {
        return key
    }

    internal fun bind(holderSet: HolderSet.Named<V>?) {
        contents = holderSet
    }
}