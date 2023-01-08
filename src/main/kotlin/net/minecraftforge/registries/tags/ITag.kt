package net.minecraftforge.registries.tags

import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import java.util.Optional
import java.util.stream.Stream

interface ITag<V> : Iterable<V> {
    val key: TagKey<V>

    fun stream(): Stream<V>
    fun isEmpty(): Boolean
    fun size(): Int
    fun contains(value: V): Boolean
    fun getRandomElement(random: RandomSource): Optional<V>
    fun isBound(): Boolean
}