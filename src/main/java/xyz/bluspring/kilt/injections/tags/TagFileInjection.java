package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

@FabricInjectedInterface(TagFile.class)
public interface TagFileInjection {
    default List<TagEntry> remove() {
        throw KiltHelper.createMixinException(TagFile.class, "remove");
    }

    default void kilt$setRemove(List<TagEntry> remove) {
        throw KiltHelper.createMixinException(TagFile.class, "kilt$setRemove");
    }
}
