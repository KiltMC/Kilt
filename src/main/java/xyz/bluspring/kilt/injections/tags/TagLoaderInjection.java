package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

public interface TagLoaderInjection {
    @FabricInjectedInterface(TagLoader.EntryWithSource.class)
    public interface EntryWithSourceInjection {
        static TagLoader.EntryWithSource create(TagEntry entry, String source, boolean remove) {
            var entryWithSource = new TagLoader.EntryWithSource(entry, source);
            ((EntryWithSourceInjection) (Object) entryWithSource).kilt$setRemove(remove);

            return entryWithSource;
        }

        default boolean remove() {
            throw KiltHelper.createMixinException(TagLoader.EntryWithSource.class, "remove");
        }

        default void kilt$setRemove(boolean remove) {
            throw KiltHelper.createMixinException(TagLoader.EntryWithSource.class, "kilt$setRemove");
        }
    }
}
