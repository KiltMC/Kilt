package xyz.bluspring.kilt.injections.tags;

import net.minecraft.resources.ResourceLocation;

public interface TagEntryInjection {
    ResourceLocation getId();
    boolean isRequired();
    boolean isTag();
}
