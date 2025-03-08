package xyz.bluspring.kilt.forgeinjects.data;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BuiltinRegistries.class)
public abstract class BuiltinRegistriesInject {

    @Shadow
    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> registryKey, R registry, BuiltinRegistries.RegistryBootstrap<T> bootstrap, Lifecycle lifecycle) {
        throw new IllegalStateException();
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/BuiltinRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/data/BuiltinRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;", ordinal = 8))
    private static <T> Registry<T> kilt$registerForge(ResourceKey<? extends Registry<T>> registryKey, BuiltinRegistries.RegistryBootstrap<T> bootstrap) {
        return internalRegister(registryKey, GameData.getWrapper(registryKey, Lifecycle.stable()), bootstrap, Lifecycle.stable());
    }
}
