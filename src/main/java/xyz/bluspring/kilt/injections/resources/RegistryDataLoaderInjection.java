package xyz.bluspring.kilt.injections.resources;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.RegistryBuilder;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;

public interface RegistryDataLoaderInjection {
    interface RegistryDataInjection<T> {
        default Consumer<RegistryBuilder<T>> registryBuilderConsumer() {
            throw KiltHelper.createMixinException(RegistryDataInjection.class, "registryBuilderConsumer");
        }

        static <T> RegistryDataLoader.RegistryData<T> create(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, RegistryValidator<T> validator, Consumer<RegistryBuilder<T>> registryBuilderConsumer) {
            var data = new RegistryDataLoader.RegistryData<>(key, elementCodec, validator);
            RecordMixinRefMaps.EXTENDED_REGISTRY_DATA.put(data, new RecordMixinRefMaps.ExtendedRegistryData<>(registryBuilderConsumer));
            return data;
        }
    }
}
