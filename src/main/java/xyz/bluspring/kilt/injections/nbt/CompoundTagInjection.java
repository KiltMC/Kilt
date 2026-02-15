package xyz.bluspring.kilt.injections.nbt;

import net.minecraft.nbt.CompoundTag;
import xyz.bluspring.kilt.mixin.CompoundTagAccessor;

import java.util.HashMap;

public interface CompoundTagInjection {
    static CompoundTag create(int expectedEntries) {
        return CompoundTagAccessor.createCompoundTag(HashMap.newHashMap(expectedEntries));
    }
}
