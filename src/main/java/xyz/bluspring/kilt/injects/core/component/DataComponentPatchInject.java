package xyz.bluspring.kilt.injects.core.component;

import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.component.DataComponentPatchInjection;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.class)
public abstract class DataComponentPatchInject implements DataComponentPatchInjection {
    @Shadow @Final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    @Override
    public @Nullable <T> Optional<T> getPatch(DataComponentType<? extends T> type) {
        return (Optional<T>) this.map.get(type);
    }

    // Kilt: we don't have to implement component validation
}
