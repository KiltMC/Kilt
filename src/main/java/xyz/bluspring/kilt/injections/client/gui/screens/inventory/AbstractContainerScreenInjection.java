package xyz.bluspring.kilt.injections.client.gui.screens.inventory;

import net.minecraft.world.inventory.Slot;

public interface AbstractContainerScreenInjection {
    Slot getSlotUnderMouse();
    int getGuiLeft();
    int getGuiTop();
    int getXSize();
    int getYSize();
    int getSlotColor(int index);
    void kilt$setSlotColor(int slotColor);
}
