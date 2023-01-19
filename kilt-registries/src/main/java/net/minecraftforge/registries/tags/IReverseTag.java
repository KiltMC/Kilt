package net.minecraftforge.registries.tags;

import net.minecraft.tags.TagKey;
import java.util.stream.Stream;

public interface IReverseTag<V> {
    default Stream<TagKey<V>> getTagKeys() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default boolean containsTag(TagKey<V> key) {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default boolean containsTag(ITag<V> tag) {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}