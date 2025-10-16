package xyz.bluspring.kilt.injections.resources;

import net.neoforged.neoforge.registries.RegistryBuilder;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface RegistryDataLoader$RegistryDataInjection<T> {
    default Consumer<RegistryBuilder<T>> registryBuilderConsumer() {
        throw KiltHelper.createMixinException(RegistryDataLoader$RegistryDataInjection.class, "registryBuilderConsumer");
    }

    default void kilt$setRegistryBuilderConsumer(Consumer<RegistryBuilder<T>> builderConsumer) {
        throw KiltHelper.createMixinException(RegistryDataLoader$RegistryDataInjection.class, "kilt$setRegistryBuilderConsumer");
    }
}
