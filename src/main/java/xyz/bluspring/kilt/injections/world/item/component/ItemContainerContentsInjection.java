package xyz.bluspring.kilt.injections.world.item.component;

import net.minecraft.world.item.ItemStack;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ItemContainerContentsInjection {
    default int getSlots() {
        throw KiltHelper.createMixinException(ItemContainerContentsInjection.class, "getSlots");
    }

    default ItemStack getStackInSlot(int slot) {
        throw KiltHelper.createMixinException(ItemContainerContentsInjection.class, "getStackInSlot");
    }
}
