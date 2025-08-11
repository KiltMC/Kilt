// TRACKED HASH: 71cfb66f5af012480c2b566dde1de58adb0aeede
package xyz.bluspring.kilt.injects.server;

import net.minecraft.server.Bootstrap;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapInject {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;validate()V", shift = At.Shift.AFTER))
    private static void kilt$initForgeHooks(CallbackInfo ci) {
        GameData.vanillaSnapshot();

        // Forge: Hacky fix to ensure that NetworkConstants is loaded before mods are constructed.
        // Many older mods use network internals that shouldn't be used, yet are exposed so they get used anyways.
        // This can cause class-loading issues with ForgeMod loading NetworkConstants and HandshakeResolver.
        // To ensure that doesn't happen, we load it here and now. This is not an issue in 1.20.2 and newer.
        // Kilt: ^ yeah what he said
        NetworkHooks.init();
    }
}