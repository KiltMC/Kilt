// TRACKED HASH: ea9681f393c5ebdcf5011d9367e1033462ba9c4c
package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.IReverseTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.HolderReferenceInjection;

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

    @Mixin(Holder.Reference.class)
    class ReferenceInject implements HolderReferenceInjection {
        @Shadow @Final
        private Holder.Reference.Type type;

        @Override
        @NotNull
        public Holder.Reference.Type getType() {
            return this.type;
        }
    }
}