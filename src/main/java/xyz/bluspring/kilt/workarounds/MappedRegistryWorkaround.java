package xyz.bluspring.kilt.workarounds;

import net.minecraft.resources.ResourceLocation;

public interface MappedRegistryWorkaround {

    void addAlias(ResourceLocation old, ResourceLocation newId);

}
