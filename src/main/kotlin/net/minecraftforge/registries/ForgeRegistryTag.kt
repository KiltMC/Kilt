package net.minecraftforge.registries

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraftforge.registries.tags.ITag
import java.util.*
import java.util.stream.Stream

class ForgeRegistryTag<V> internal constructor(
    override val key: TagKey<V>
) : ITag<V> {
    private var contents: HolderSet<V>? = null

    internal constructor(key: TagKey<V>, contents: HolderSet.Named<V>) : this(key) {
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

        // apparently because V is nullable, this will become an error
        // in future versions of Kotlin.
        // but I'm not sure how else I'd do this.
        return Optional.ofNullable(
            if (randomElement?.isPresent == true)
                randomElement.get().value()
            else null
        )
    }

    override fun isBound(): Boolean {
        return contents != null
    }

    override fun contains(value: V): Boolean {
        return contents?.contains(Holder.direct(value)) == true
    }

    override fun iterator(): Iterator<V> {
        return if (contents == null)
            listOf<V>().iterator()
        else
            contents!!.map { it.value() }.iterator()
    }
}