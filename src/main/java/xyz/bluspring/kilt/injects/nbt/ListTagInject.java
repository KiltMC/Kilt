package xyz.bluspring.kilt.injects.nbt;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

@Mixin(ListTag.class)
public abstract class ListTagInject {
    public ListTagInject(List<Tag> list) {}

    @CreateInitializer
    public ListTagInject(int initialCapacity) {
        this(new ArrayList<>(initialCapacity));
    }
}
