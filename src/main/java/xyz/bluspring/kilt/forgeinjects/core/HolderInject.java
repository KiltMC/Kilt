package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.IReverseTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(Holder.class)
public interface HolderInject<T> extends IReverseTag<T>, Supplier<T> {
    @Shadow boolean is(TagKey<T> resourceKey);

    @Shadow Stream<TagKey<T>> tags();

    @Shadow T value();

    @Override
    default boolean containsTag(@NotNull TagKey<T> key) {
        return this.is(key);
    }

    @NotNull
    @Override
    default Stream<TagKey<T>> getTagKeys() {
        return this.tags();
    }

    @Override
    default T get() {
        return this.value();
    }
}
