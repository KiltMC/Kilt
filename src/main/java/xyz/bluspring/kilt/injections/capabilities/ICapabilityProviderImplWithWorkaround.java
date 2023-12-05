package xyz.bluspring.kilt.injections.capabilities;

import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

public interface ICapabilityProviderImplWithWorkaround<B extends ICapabilityProviderImpl<B>> extends ICapabilityProviderImpl<B> {
    default CapabilityProviderWorkaround<B> getWorkaround() {
        throw new IllegalStateException();
    }
}
