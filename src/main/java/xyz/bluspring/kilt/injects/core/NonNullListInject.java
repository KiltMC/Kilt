package xyz.bluspring.kilt.injects.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.NonNullListInjection;

import java.util.Collection;

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
