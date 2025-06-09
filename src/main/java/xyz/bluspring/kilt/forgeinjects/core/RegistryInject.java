package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Registry;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registry.class)
public abstract class RegistryInject {
    /*@ModifyVariable(method = "internalRegister", at = @At("HEAD"), argsOnly = true)
    private static <T, R extends WritableRegistry<T>> R kilt$wrapWithGameDataWrapper(R registry, @Local(argsOnly = true) ResourceKey<? extends Registry<T>> key, @Local(argsOnly = true) Lifecycle lifecycle) {
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

}
