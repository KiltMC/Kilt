package xyz.bluspring.kilt.injects.core.registries;

import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

@Mixin(Registries.class)
public abstract class RegistriesInject {
    @Inject(method = "elementsDirPath", at = @At("HEAD"), cancellable = true)
    private static void kilt$checkPrefixNamespace(ResourceKey<? extends Registry<?>> registryKey, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(CommonHooks.prefixNamespace(registryKey.location()));
    }

    // Kilt: Handled by Fabric API
}
