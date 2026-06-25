package xyz.bluspring.kilt.compat.fabric.mixin.everycompat;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.every_compat.EveryCompat;
import net.mehvahdjukaar.every_compat.EveryCompatCommon;
import net.mehvahdjukaar.every_compat.api.CompatModule;
import net.mehvahdjukaar.every_compat.neoforge.EveryCompatForge;
import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EveryCompatForge.class)
public abstract class EveryCompatForgeMixin extends EveryCompatCommon {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/every_compat/neoforge/EveryCompatForge;initialize()V"))
    private void kilt$every_compat$avoidInitializing(EveryCompatForge instance) {
        this.addModules(); // Kilt: We only want the modules in this case.
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;register(Ljava/lang/Object;)V"))
    private void kilt$every_compat$avoidEventRegister(IEventBus instance, Object o) {}

    @Dynamic
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/platform/PlatHelper$Side;isClient()Z"))
    private boolean kilt$every_compat$disableClientInit(@Coerce Object instance) {
        return false;
    }

    @WrapOperation(method = "addModules", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/every_compat/EveryCompat;addOptionalModule(Ljava/lang/String;Ljava/util/function/Supplier;)V"))
    private void kilt$every_compat$avoidModuleRegisterIfExists(String modId, Supplier<Class<? extends CompatModule>> moduleClass, Operation<Void> original) {
        if (EveryCompat.getModulesOfMod(modId).isEmpty()) {
            original.call(modId, moduleClass);
        }
    }
}
