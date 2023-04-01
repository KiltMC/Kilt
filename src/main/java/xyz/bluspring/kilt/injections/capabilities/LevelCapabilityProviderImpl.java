package xyz.bluspring.kilt.injections.capabilities;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

public interface LevelCapabilityProviderImpl extends ICapabilityProviderImpl<Level> {
    CapabilityProviderWorkaround<Level> getWorkaround();
}
