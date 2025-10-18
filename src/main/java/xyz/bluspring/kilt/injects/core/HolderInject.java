// TRACKED HASH: ea9681f393c5ebdcf5011d9367e1033462ba9c4c
package xyz.bluspring.kilt.injects.core;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.HolderLookup$RegistryLookupInjection;

import java.util.stream.Stream;

@Mixin(Holder.class)
public interface HolderInject<T> extends IHolderExtension<T> {
    @Shadow boolean is(TagKey<T> resourceKey);
    @Shadow Stream<TagKey<T>> tags();
    @Shadow T value();

    @Mixin(Holder.Reference.class)
    abstract class ReferenceInject<T> implements IHolderExtension<T> {
        @Shadow @Nullable private ResourceKey<T> key;
        @Shadow public abstract ResourceKey<T> key();
        @Shadow @Final private HolderOwner<T> owner;

        @Intrinsic
        @Override
        public <T1> @Nullable T1 getData(DataMapType<T, T1> type) {
            if (owner instanceof HolderLookup.RegistryLookup<T> lookup)
                return ((HolderLookup$RegistryLookupInjection<T>) lookup).getData(type, this.key());

            return null;
        }

        @Intrinsic
        @Override
        public @Nullable ResourceKey<T> getKey() {
            return this.key;
        }

        @Intrinsic
        @Override
        public int hashCode() {
            return this.key().hashCode();
        }

        @Intrinsic
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            return obj instanceof Holder<?> h && h.kind() == Holder.Kind.REFERENCE && ((IHolderExtension<T>) h).getKey() == this.key();
        }

        @Intrinsic
        @Override
        public HolderLookup.@Nullable RegistryLookup<T> unwrapLookup() {
            return this.owner instanceof HolderLookup.RegistryLookup<T> rl ? rl : null;
        }
    }
}