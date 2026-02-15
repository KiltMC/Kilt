package xyz.bluspring.kilt.injects.nbt;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.ArrayList;
import java.util.List;

@Mixin(ListTag.class)
public abstract class ListTagInject {
    ListTagInject(List<Tag> list, byte type) {}

    @CreateInitializer
    public ListTagInject(int initialCapacity) {
        this(new ArrayList<>(initialCapacity), (byte) 0);
    }
}
