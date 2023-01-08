package net.minecraftforge.registries.tags

import net.minecraft.tags.TagKey
import java.util.stream.Stream

interface IReverseTag<V> {
    fun getTagKeys(): Stream<TagKey<V>>
    fun containsTag(key: TagKey<V>): Boolean
    fun containsTag(tag: ITag<V>): Boolean {
        return containsTag(tag.key)
    }
}