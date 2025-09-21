package xyz.bluspring.kilt.injections.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(TagEntry.class)
public interface TagEntryInjection {
    default ResourceLocation getId() {
        throw KiltHelper.createMixinException(TagEntry.class, "getId");
    }

    default boolean isRequired() {
        throw KiltHelper.createMixinException(TagEntry.class, "isRequired");
    }

    default boolean isTag() {
        throw KiltHelper.createMixinException(TagEntry.class, "isTag");
    }
}
