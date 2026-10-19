package xyz.bluspring.kilt.injections.core.component;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponentType;

public interface DataComponentPatchInjection {
    default <T> @Nullable Optional<T> getPatch(DataComponentType<? extends T> type) {
        throw KiltHelper.createMixinException(DataComponentPatchInjection.class, "getPatch");
    }
}
