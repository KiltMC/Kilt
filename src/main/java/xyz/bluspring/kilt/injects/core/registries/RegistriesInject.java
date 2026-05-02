package xyz.bluspring.kilt.injects.core.registries;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.registries.Registries;

@Mixin(Registries.class)
public abstract class RegistriesInject {
    // Kilt: DO NOT BY ANY MEANS USE THIS, IT WILL CAUSE PROBLEMS. (#743, #161, #187)
//    @Inject(method = "elementsDirPath", at = @At("HEAD"), cancellable = true)
//    private static void kilt$checkPrefixNamespace(ResourceKey<? extends Registry<?>> registryKey, CallbackInfoReturnable<String> cir) {
//        cir.setReturnValue(CommonHooks.prefixNamespace(registryKey.location()));
//    }

    // Kilt: Handled by Fabric API
}
