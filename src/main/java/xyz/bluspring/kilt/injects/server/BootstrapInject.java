// TRACKED HASH: 71cfb66f5af012480c2b566dde1de58adb0aeede
package xyz.bluspring.kilt.injects.server;

import net.minecraft.server.Bootstrap;
import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapInject {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;validate()V", shift = At.Shift.AFTER))
    private static void kilt$initForgeHooks(CallbackInfo ci) {
        GameData.vanillaSnapshot();
    }
}