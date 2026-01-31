package xyz.bluspring.kilt.injections.resources;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface RegistryDataLoaderInjection {
    interface RegistryDataInjection<T> {
        default Consumer<RegistryBuilder<T>> registryBuilderConsumer() {
            throw KiltHelper.createMixinException(RegistryDataInjection.class, "registryBuilderConsumer");
        }

        default void kilt$setRegistryBuilderConsumer(Consumer<RegistryBuilder<T>> builderConsumer) {
            throw KiltHelper.createMixinException(RegistryDataInjection.class, "kilt$setRegistryBuilderConsumer");
        }

        static <T> RegistryDataLoader.RegistryData<T> create(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty, Consumer<RegistryBuilder<T>> builderConsumer) {
            var registryData = new RegistryDataLoader.RegistryData<>(key, elementCodec, requiredNonEmpty);
            registryData.kilt$setRegistryBuilderConsumer(builderConsumer);
            return registryData;
        }
    }
}
