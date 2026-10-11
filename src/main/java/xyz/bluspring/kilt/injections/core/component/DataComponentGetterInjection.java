package xyz.bluspring.kilt.injections.core.component;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponentType;

public interface DataComponentGetterInjection {
    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> componentType) {
        throw KiltHelper.createMixinException(DataComponentGetterInjection.class, "get");
    }

    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> componentType, T value) {
        throw KiltHelper.createMixinException(DataComponentGetterInjection.class, "getOrDefault");
    }

    default <T> boolean has(Supplier<? extends DataComponentType<? extends T>> componentType) {
        throw KiltHelper.createMixinException(DataComponentGetterInjection.class, "has");
    }

    default boolean has(DataComponentType<?> componentType) {
        throw KiltHelper.createMixinException(DataComponentGetterInjection.class, "has");
    }
}
