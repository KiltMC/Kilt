package xyz.bluspring.kilt.workarounds;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public interface MutableDataComponentHolderWorkaround {

    <T> T set(@NotNull DataComponentType<? super T> componentType, @Nullable T value);

    <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater);

    <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater);

    <T> T remove(@NotNull DataComponentType<? super T> componentType);

    void applyComponents(@NotNull DataComponentPatch patch);

    void applyComponents(@NotNull DataComponentMap components);
}
