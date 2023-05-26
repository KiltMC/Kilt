package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenInject {

    @Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper")
    public static class SlotWrapperInject implements SlotInjection {
        @Shadow @Final
        Slot target;

        @Override
        public int getSlotIndex() {
            return this.target.getSlotIndex();
        }

        @Override
        public boolean isSameInventory(Slot other) {
            return ((SlotInjection) this.target).isSameInventory(other);
        }

        @Override
        public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
            return this.target.setBackground(atlas, sprite);
        }
    }
}
