package xyz.bluspring.kilt.forgeinjects.server.rcon;

import net.minecraft.network.chat.Component;
import net.minecraft.server.rcon.RconConsoleSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RconConsoleSource.class)
public abstract class RconConsoleSourceInject {
    @Shadow @Final private StringBuffer buffer;

    @Inject(method = "sendSystemMessage", at = @At("TAIL"))
    private void kilt$fixMC7569(Component component, CallbackInfo ci) {
        this.buffer.append("\n"); // Kilt: *sigh* why not, i guess.
    }
}
