package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagEntry;

import java.util.List;

public interface TagFileInjection {
    List<TagEntry> remove();
    void kilt$setRemove(List<TagEntry> remove);
}
