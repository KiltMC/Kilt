package xyz.bluspring.kilt.injections.tags;

import net.minecraft.tags.TagEntry;

import java.util.List;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;

public interface TagFileInjection extends FabricTagFile {
    List<TagEntry> remove();
    void kilt$setRemove(List<TagEntry> remove);
}
