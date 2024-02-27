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
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesInject {
    @ModifyArg(method = "internalRegister", at = @At("HEAD"))
    private static <T, R extends WritableRegistry<T>> R kilt$wrapWithGameDataWrapper(R registry, @Local(argsOnly = true) ResourceKey<? extends Registry<T>> key, @Local(argsOnly = true) BuiltInRegistries.RegistryBootstrap<T> bootstrap, @Local(argsOnly = true) Lifecycle lifecycle) {
        if (registry instanceof DefaultedRegistry<?> defaulted)
            return (R) GameData.getWrapper(key, lifecycle, defaulted.getDefaultKey().toString());

        return (R) GameData.getWrapper(key, lifecycle);
    }
}
