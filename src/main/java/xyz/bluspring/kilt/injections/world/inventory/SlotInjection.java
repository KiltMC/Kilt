package xyz.bluspring.kilt.injections.world.inventory;

import io.github.fabricators_of_create.porting_lib.extensions.common.SlotExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public interface SlotInjection extends SlotExtension {
    default int getSlotIndex() {
        return port_lib$getSlotIndex();
    }

    default boolean isSameInventory(Slot other) {
        return port_lib$isSameInventory(other);
    }

    default Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        return port_lib$setBackground(atlas, sprite);
    }
}
