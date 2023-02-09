package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(RegistryAccess.class)
public interface RegistryAccessInject {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;make(Ljava/util/function/Supplier;)Ljava/lang/Object;"), method = "<clinit>")
    private static <T> T kilt$grabBuiltinRegistries(Supplier<T> supplier) {
        return (T) Util.make(() -> DataPackRegistriesHooks.grabBuiltinRegistries((Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>>) supplier.get()));
    }
}
