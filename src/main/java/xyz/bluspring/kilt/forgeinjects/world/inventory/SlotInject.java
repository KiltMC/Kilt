// TRACKED HASH: 5bd8da3c51f4d0280c72682973eb6e5b67353690
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