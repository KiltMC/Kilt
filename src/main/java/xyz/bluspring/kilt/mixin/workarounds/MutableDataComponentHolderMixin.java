package xyz.bluspring.kilt.mixin.workarounds;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.workarounds.MutableDataComponentHolderWorkaround;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

@Implements(@Interface(iface = MutableDataComponentHolderWorkaround.class, prefix = "kilt$i$", remap = Interface.Remap.NONE))
@Mixin(MutableDataComponentHolder.class)
public interface MutableDataComponentHolderMixin {

    @Shadow
    @Nullable
    <T> T set(DataComponentType<? super T> dataComponentType, @Nullable T object);

    @Shadow
    @Nullable <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater);

    @Shadow
    @Nullable <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater);

    @Shadow
    @Nullable
    <T> T remove(DataComponentType<? extends T> dataComponentType);

    @Shadow
    void applyComponents(DataComponentPatch patch);

    @Shadow
    void applyComponents(DataComponentMap dataComponentMap);

    @Intrinsic
    default <T> T kilt$i$set(@NotNull DataComponentType<? super T> componentType, @Nullable T value) {
        return this.set(componentType, value);
    }

    @Intrinsic
    default <T> T kilt$i$remove(@NotNull DataComponentType<? super T> componentType) {
        //noinspection unchecked
        return this.remove((DataComponentType<? extends T>) componentType);
    }

    @Intrinsic
    default <T, U> T kilt$i$update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return this.update(componentType, value, updateContext, updater);
    }

    @Intrinsic
    default <T> T kilt$i$update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
        return this.update(componentType, value, updater);
    }

    @Intrinsic
    default void kilt$i$applyComponents(@NotNull DataComponentPatch patch) {
        this.applyComponents(patch);
    }

    @Intrinsic
    default void kilt$i$applyComponents(@NotNull DataComponentMap components) {
        this.applyComponents(components);
    }
}
