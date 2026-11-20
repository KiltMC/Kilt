// TRACKED HASH: 36647a2e1ac76fa3c6da6c09a3707cd87384c62a
package xyz.bluspring.kilt.injects.client.main;

import net.neoforged.neoforge.client.loading.ClientModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.main.Main;

@Mixin(Main.class)
public abstract class MainInject {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Bootstrap;validate()V", shift = At.Shift.AFTER))
    private static void kilt$initClientModLoader(String[] args, CallbackInfo ci) {
        ClientModLoader.begin();
    }
}
