package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.nbt.CompoundTag;

public interface ItemStackInjection {
    default CompoundTag getCapNBT() {
        throw new IllegalStateException();
    }
}
