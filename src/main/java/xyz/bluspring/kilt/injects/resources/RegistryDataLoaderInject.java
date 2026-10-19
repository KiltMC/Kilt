package xyz.bluspring.kilt.injects.resources;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.resources.RegistryDataLoaderInjection;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderInject {
    @Shadow @Final @Mutable public static List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$grabNetworkableRegistries(CallbackInfo ci) {
        SYNCHRONIZED_REGISTRIES = DataPackRegistriesHooks.grabNetworkableRegistries(SYNCHRONIZED_REGISTRIES);
    }

    @Mixin(RegistryDataLoader.RegistryData.class)
    public abstract static class RegistryDataInject<T> implements RegistryDataLoaderInjection.RegistryDataInjection<T> {
        public RegistryDataInject(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, RegistryValidator<T> validator) {}

        @CreateInitializer
        public RegistryDataInject(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, RegistryValidator<T> validator, Consumer<RegistryBuilder<T>> registryBuilderConsumer) {
            this(key, elementCodec, validator);
            RecordMixinRefMaps.EXTENDED_REGISTRY_DATA.put((RegistryDataLoader.RegistryData<?>) (Object) this, new RecordMixinRefMaps.ExtendedRegistryData<>(registryBuilderConsumer));
        }

        @Override
        public Consumer<RegistryBuilder<T>> registryBuilderConsumer() {
            var extension = RecordMixinRefMaps.EXTENDED_REGISTRY_DATA.get((RegistryDataLoader.RegistryData<?>) (Object) this);
            if (extension != null)
                return (Consumer<RegistryBuilder<T>>) (Object) extension.registryBuilderConsumer();

            return _ -> {};
        }
    }
}
