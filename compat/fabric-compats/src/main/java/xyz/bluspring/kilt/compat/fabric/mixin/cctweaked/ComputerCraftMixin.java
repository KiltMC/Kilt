package xyz.bluspring.kilt.compat.fabric.mixin.cctweaked;

import dan200.computercraft.shared.ComputerCraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.KiltLoader;

@Mixin(ComputerCraft.class)
public abstract class ComputerCraftMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private static void kilt$addCreateCompat(CallbackInfo ci) {
        if (KiltLoader.Companion.getInstance().hasMod("create")) {
            try {
                // we can't have CC:T forge in compile classpath because it overwrites CC:T fabric classes
                // use reflection instead
                Class.forName("dan200.computercraft.shared.integration.CreateIntegration").getMethod("setup").invoke(null);
            } catch (Exception e) {
                Kilt.Companion.getLogger().error("Failed to initialize CC:T Create integration!");
            }
        }
    }
}
