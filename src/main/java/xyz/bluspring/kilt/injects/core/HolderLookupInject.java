package xyz.bluspring.kilt.injects.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.extensions.IHolderLookupProviderExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.HolderLookup$RegistryLookupInjection;

@Mixin(HolderLookup.class)
public interface HolderLookupInject {

    @Mixin(HolderLookup.Provider.class)
    public interface ProviderInject extends IHolderLookupProviderExtension {
    }

    @Mixin(HolderLookup.RegistryLookup.class)
    public interface RegistryLookupInject<T> extends HolderLookup$RegistryLookupInjection<T> {
        @Intrinsic
        @Override
        default <A> @Nullable A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
            return null;
        }

        @Mixin(HolderLookup.RegistryLookup.Delegate.class)
        public interface DelegateInject<T> extends HolderLookup$RegistryLookupInjection<T> {
            @Shadow HolderLookup.RegistryLookup<T> parent();

            @Override
            default <A> @Nullable A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
                return ((HolderLookup$RegistryLookupInjection<T>) this.parent()).getData(attachment, key);
            }
        }
    }
}
