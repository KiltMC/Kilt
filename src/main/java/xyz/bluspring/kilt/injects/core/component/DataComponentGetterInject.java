package xyz.bluspring.kilt.injects.core.component;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.component.DataComponentGetterInjection;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentGetter.class)
public interface DataComponentGetterInject extends DataComponentGetterInjection {
    @Shadow <T> T get(DataComponentType<? extends T> type);
    @Shadow <T> T getOrDefault(DataComponentType<? extends T> type, T defaultValue);

    @Override
    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return this.get(componentType.get());
    }

    @Override
    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> componentType, T value) {
        return this.getOrDefault(componentType.get(), value);
    }

    @Override
    default <T> boolean has(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return this.get(componentType) != null;
    }

    @Override
    default boolean has(DataComponentType<?> componentType) {
        return this.get(componentType) != null;
    }
}
