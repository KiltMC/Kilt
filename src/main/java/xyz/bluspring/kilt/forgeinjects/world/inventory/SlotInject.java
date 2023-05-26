package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;

@Mixin(Slot.class)
public abstract class SlotInject implements SlotInjection {
    @Shadow @Final public Container container;

    @Override
    public boolean isSameInventory(Slot other) {
        return this.container == other.container;
    }
}
