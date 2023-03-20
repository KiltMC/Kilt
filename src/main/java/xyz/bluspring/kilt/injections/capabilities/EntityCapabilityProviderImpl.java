package xyz.bluspring.kilt.injections.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

public interface EntityCapabilityProviderImpl extends ICapabilityProviderImpl<Entity> {
    CapabilityProviderWorkaround<Entity> getWorkaround();
}
