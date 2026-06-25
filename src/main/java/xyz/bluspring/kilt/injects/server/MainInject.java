package xyz.bluspring.kilt.injects.server;

import net.neoforged.neoforge.server.loading.ServerModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Main;

@Mixin(Main.class)
public abstract class MainInject {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;startTimerHackThread()V", shift = At.Shift.AFTER))
    private static void kilt$initForgeLoader(String[] strings, CallbackInfo ci) {
        ServerModLoader.load(false);
    }

    // TODO: oh jesus christ good luck.
}
