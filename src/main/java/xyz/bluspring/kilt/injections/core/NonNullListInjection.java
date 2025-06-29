package xyz.bluspring.kilt.injections.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import xyz.bluspring.kilt.mixin.core.NonNullListAccessor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface NonNullListInjection {
    static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(NonNullListInjection::copyOf, Function.identity());
    }

    static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
        return NonNullListAccessor.createNonNullList(List.copyOf(entries), null);
    }
}
