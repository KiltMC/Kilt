package xyz.bluspring.kilt.injections.nbt;

import java.util.ArrayList;

import xyz.bluspring.kilt.mixin.ListTagAccessor;

import net.minecraft.nbt.ListTag;

public interface ListTagInjection {
    static ListTag create(int initialCapacity) {
        return ListTagAccessor.createListTag(new ArrayList<>(initialCapacity));
    }
}
