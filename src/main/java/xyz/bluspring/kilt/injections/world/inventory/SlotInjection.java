package xyz.bluspring.kilt.injections.world.inventory;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

public interface SlotInjection {
    default int getSlotIndex() {
        throw KiltHelper.createMixinException(SlotInjection.class, "getSlotIndex");
    }

    default boolean isSameInventory(Slot other) {
        throw KiltHelper.createMixinException(SlotInjection.class, "isSameInventory");
    }

    default Slot setBackground(Identifier atlas, Identifier sprite) {
        throw KiltHelper.createMixinException(SlotInjection.class, "setBackground");
    }
}
