package xyz.bluspring.kilt.injections.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

public interface ItemStackCapabilityProviderImpl extends ICapabilityProviderImpl<ItemStack> {
    default CapabilityProviderWorkaround<ItemStack> getWorkaround() {
        throw new IllegalStateException();
    }

    default boolean areCapsCompatible(ICapabilityProviderImpl<ItemStack> stack) {
        throw new IllegalStateException();
    }
}
