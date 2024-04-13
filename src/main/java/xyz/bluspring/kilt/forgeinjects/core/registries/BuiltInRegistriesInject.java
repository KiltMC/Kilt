// TRACKED HASH: 33c37e1450e21ad82101b30b9cc1bf7f4cb0c12d
package xyz.bluspring.kilt.forgeinjects.core.registries;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesInject {
    @ModifyVariable(method = "internalRegister", at = @At("HEAD"), argsOnly = true)
    private static <T, R extends WritableRegistry<T>> R kilt$wrapWithGameDataWrapper(R registry, @Local(argsOnly = true) ResourceKey<? extends Registry<T>> key, @Local(argsOnly = true) BuiltInRegistries.RegistryBootstrap<T> bootstrap, @Local(argsOnly = true) Lifecycle lifecycle) {
        R wrapper;
        if (registry instanceof DefaultedRegistry<?> defaulted)
            wrapper = (R) GameData.getWrapper(key, lifecycle, defaulted.getDefaultKey().toString());
        else
            wrapper = (R) GameData.getWrapper(key, lifecycle);

        if (wrapper == null)
            return registry;
        else
            return wrapper;
    }
}