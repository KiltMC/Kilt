package xyz.bluspring.kilt.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ListTag.class)
public interface ListTagAccessor {
    @Invoker("<init>")
    static ListTag createListTag(List<Tag> list, byte type) {
        throw new UnsupportedOperationException();
    }
}
