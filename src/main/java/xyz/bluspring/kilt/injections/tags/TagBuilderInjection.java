package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.stream.Stream;

@FabricInjectedInterface(TagBuilder.class)
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

    default boolean isReplace() {
        throw KiltHelper.createMixinException(TagBuilderInjection.class, "isReplace");
    }
}
