package xyz.bluspring.kilt.injections.tags;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

@FabricInjectedInterface(TagEntry.class)
public interface TagEntryInjection {
    default Identifier getId() {
        throw KiltHelper.createMixinException(TagEntry.class, "getId");
    }

    default boolean isRequired() {
        throw KiltHelper.createMixinException(TagEntry.class, "isRequired");
    }

    default boolean isTag() {
        throw KiltHelper.createMixinException(TagEntry.class, "isTag");
    }
}
