package xyz.bluspring.kilt.injects.server;

import net.minecraft.server.Main;
import net.neoforged.neoforge.server.loading.ServerModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MainInject {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;startTimerHackThread()V", shift = At.Shift.AFTER))
    private static void kilt$initForgeLoader(String[] strings, CallbackInfo ci) {
        ServerModLoader.load();
    }

    // TODO: oh jesus christ good luck.
}
