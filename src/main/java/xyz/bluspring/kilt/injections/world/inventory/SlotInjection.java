package xyz.bluspring.kilt.injections.world.inventory;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.SlotExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public interface SlotInjection extends SlotExtensions {
    int getSlotIndex();
    boolean isSameInventory(Slot other);
    Slot setBackground(ResourceLocation atlas, ResourceLocation sprite);
}
