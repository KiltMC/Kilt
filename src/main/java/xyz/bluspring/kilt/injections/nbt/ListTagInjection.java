package xyz.bluspring.kilt.injections.nbt;

import net.minecraft.nbt.ListTag;
import xyz.bluspring.kilt.mixin.ListTagAccessor;

import java.util.ArrayList;

public interface ListTagInjection {
    static ListTag create(int initialCapacity) {
        return ListTagAccessor.createListTag(new ArrayList<>(initialCapacity), (byte) 0);
    }
}
