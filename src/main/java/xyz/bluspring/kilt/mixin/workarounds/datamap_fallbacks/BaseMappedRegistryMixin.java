package xyz.bluspring.kilt.mixin.workarounds.datamap_fallbacks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Mixin(BaseMappedRegistry.class)
public abstract class BaseMappedRegistryMixin<T> implements Registry<T> {
    @ModifyReturnValue(method = "getData", at = @At("RETURN"))
    private <A> A kilt$tryUseFallback(@Nullable A original, @Local(argsOnly = true) DataMapType<T, A> type, @Local(argsOnly = true) ResourceKey<T> key) {
        if (original == null) {
            var value = this.get(key);
            if (value != null) {
                return type.kilt$getFallbackFor(value);
            }
        }

        return original;
    }
}
