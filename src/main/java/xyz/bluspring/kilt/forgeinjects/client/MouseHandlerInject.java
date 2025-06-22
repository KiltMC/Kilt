package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.MouseHandlerInjection;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerInject implements MouseHandlerInjection {
    // Kilt: this should be handled by Architectury already.

    @Shadow private double accumulatedDX;

    @Shadow private double accumulatedDY;

    @Shadow @Final private Minecraft minecraft;

    @Override
    public double getXVelocity() {
        return this.accumulatedDX;
    }

    @Override
    public double getYVelocity() {
        return this.accumulatedDY;
    }

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;"), cancellable = true)
    private void kilt$onMouseButtonPre(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (ForgeHooksClient.onMouseButtonPre(button, action, modifiers)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At("TAIL"))
    private void kilt$onMouseButtonPost(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            ForgeHooksClient.onMouseButtonPost(button, action, modifiers);
        }
    }
}
