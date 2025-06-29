package xyz.bluspring.kilt.forgeinjects.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.NonNullListInjection;
import xyz.bluspring.kilt.mixin.core.NonNullListAccessor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Mixin(NonNullList.class)
public abstract class NonNullListInject implements NonNullListInjection {
    @CreateStatic
    private static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
        return NonNullListInjection.codecOf(entryCodec);
    }

    @CreateStatic
    private static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
        return NonNullListInjection.copyOf(entries);
    }
}
