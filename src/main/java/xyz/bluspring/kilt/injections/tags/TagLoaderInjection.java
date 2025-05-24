package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;

public interface TagLoaderInjection {
    public interface EntryWithSourceInjection {
        static TagLoader.EntryWithSource create(TagEntry entry, String source, boolean remove) {
            var entryWithSource = new TagLoader.EntryWithSource(entry, source);
            ((EntryWithSourceInjection) (Object) entryWithSource).kilt$setRemove(remove);

            return entryWithSource;
        }

        boolean remove();
        void kilt$setRemove(boolean remove);
    }
}
