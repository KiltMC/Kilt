package xyz.bluspring.kilt.workarounds;

import net.minecraft.resources.Identifier;

public interface MappedRegistryWorkaround {

    void addAlias(Identifier old, Identifier newId);

}
