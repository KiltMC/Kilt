package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;

import java.util.stream.Stream;

public interface TagBuilderInjection {
    default TagBuilder remove(final TagEntry entry) {
        throw new IllegalStateException();
    }

    default Stream<TagEntry> getRemoveEntries() {
        throw new IllegalStateException();
    }

    default TagBuilder replace(boolean value) {
        throw new IllegalStateException();
    }

    default TagBuilder replace() {
        return replace(true);
    }
}
