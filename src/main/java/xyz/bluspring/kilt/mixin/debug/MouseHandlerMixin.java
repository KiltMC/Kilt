package xyz.bluspring.kilt.mixin.debug;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(
        method = {"turnPlayer"},
        at = {@At(
            value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
        )}
    )
    private void kilt$turnPlayer(double d, CallbackInfo ci, @Local(ordinal = 1) double j, @Local(ordinal = 2) double k, @Local(ordinal = 3) double g, @Local(ordinal = 4) double l, @Local(ordinal = 5) double m) {
        doodad();
    }

    private void doodad() {}
}
