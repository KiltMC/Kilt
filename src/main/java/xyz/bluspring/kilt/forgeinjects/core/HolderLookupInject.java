package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.extensions.IHolderLookupProviderExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.HolderLookupInjection;

@Mixin(HolderLookup.class)
public interface HolderLookupInject {

    @Mixin(HolderLookup.Provider.class)
    public interface ProviderInject extends IHolderLookupProviderExtension {
    }

    @Mixin(HolderLookup.RegistryLookup.class)
    public interface RegistryLookupInject<T> extends HolderLookupInjection.RegistryLookupInjection<T> {
        @Intrinsic
        @Override
        default <A> @Nullable A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
            return null;
        }

        @Mixin(HolderLookup.RegistryLookup.Delegate.class)
        public interface DelegateInject<T> extends HolderLookupInjection.RegistryLookupInjection<T> {
            @Shadow HolderLookup.RegistryLookup<T> parent();

            @Override
            default <A> @Nullable A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
                return ((HolderLookupInjection.RegistryLookupInjection<T>) this.parent()).getData(attachment, key);
            }
        }
    }
}
