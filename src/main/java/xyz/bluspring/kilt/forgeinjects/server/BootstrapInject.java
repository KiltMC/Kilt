package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.server.Bootstrap;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapInject {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;freezeBuiltins()V", shift = At.Shift.AFTER))
    private static void kilt$gameDataVanillaSnapshot(CallbackInfo ci) {
        GameData.vanillaSnapshot();
        NetworkHooks.init();
    }
}