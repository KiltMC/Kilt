package xyz.bluspring.kilt.mixin.core;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(NonNullList.class)
public interface NonNullListAccessor {
    @Invoker("<init>")
    static <E> NonNullList<E> createNonNullList(List<E> list, E defaultValue) {
        throw new IllegalStateException();
    }
}
