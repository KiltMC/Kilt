package xyz.bluspring.kilt.forgeinjects.client.main;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Main.class)
public class MainInject {
    @ModifyArg(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/main/Main;run([Ljava/lang/String;Z)V"))
    private static boolean kilt$checkOptimizedDFUSetting(boolean bl) {
        // There's supposed to be a config for this, but
        // ForgeConfigAPIPort is using the class. I'll write
        // a mixin or something if it's actually needed.
        return false;
    }
}
