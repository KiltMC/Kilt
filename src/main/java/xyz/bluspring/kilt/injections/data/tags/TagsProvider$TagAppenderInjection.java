package xyz.bluspring.kilt.injections.data.tags;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(TagsProvider.TagAppender.class)
public interface TagsProvider$TagAppenderInjection {
    TagBuilder getInternalBuilder();
    String getModID();
}
