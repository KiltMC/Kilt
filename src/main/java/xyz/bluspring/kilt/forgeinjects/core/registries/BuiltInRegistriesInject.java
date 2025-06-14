// TRACKED HASH: 33c37e1450e21ad82101b30b9cc1bf7f4cb0c12d
package xyz.bluspring.kilt.forgeinjects.core.registries;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesInject {
    /*@ModifyVariable(method = "internalRegister", at = @At("HEAD"), argsOnly = true)
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
    }*/

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$initGameDataRegistries(CallbackInfo ci) {
        GameData.init();
    }

    @WrapWithCondition(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;freeze()V"))
    private static boolean kilt$avoidEarlyFreezingRegistry() {
        return false;
    }
}