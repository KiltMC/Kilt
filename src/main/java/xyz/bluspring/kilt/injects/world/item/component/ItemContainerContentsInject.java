package xyz.bluspring.kilt.injects.world.item.component;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.world.item.component.ItemContainerContentsInjection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

@Mixin(ItemContainerContents.class)
public abstract class ItemContainerContentsInject implements ItemContainerContentsInjection {
    @Shadow
    @Final
    private List<Optional<ItemStackTemplate>> items;

    @Override
    public int getSlots() {
        return this.items.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        return this.items.get(slot).map(ItemStackTemplate::create)
            .orElse(ItemStack.EMPTY);
    }

    @Unique
    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.getSlots()) {
            throw new UnsupportedOperationException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
        }
    }
}
