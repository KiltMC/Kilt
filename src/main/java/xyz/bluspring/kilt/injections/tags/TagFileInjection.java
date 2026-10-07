package xyz.bluspring.kilt.injections.tags;

import java.util.List;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;

@FabricInjectedInterface(TagFile.class)
public interface TagFileInjection extends FabricTagFile {
    default List<TagEntry> remove() {
        throw KiltHelper.createMixinException(TagFile.class, "remove");
    }

    default void kilt$setRemove(List<TagEntry> remove) {
        throw KiltHelper.createMixinException(TagFile.class, "kilt$setRemove");
    }
}
