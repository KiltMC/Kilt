package xyz.bluspring.kilt.injections.item;

import net.minecraft.nbt.CompoundTag;

public interface ItemStackInjection {
    default CompoundTag getCapNBT() {
        throw new IllegalStateException();
    }
    default void kilt$setCapNBT(CompoundTag tag) {
        throw new IllegalStateException();
    }
}
