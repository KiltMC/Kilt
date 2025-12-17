package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.ClientHooks;
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

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0), cancellable = true)
    private void kilt$onMouseButtonPre(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (ClientHooks.onMouseButtonPre(button, action, modifiers)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "method_1611", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"))
    private static boolean kilt$callScreenMouseClickEvents(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        var value = ForgeHooksClient.onScreenMouseClickedPre(instance, mouseX, mouseY, button);

        if (!value) {
            value = original.call(instance, mouseX, mouseY, button);
            value = ForgeHooksClient.onScreenMouseClickedPost(instance, mouseX, mouseY, button, value);
        }

        return value;
    }

    @WrapOperation(method = "method_1605", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(DDI)Z"))
    private static boolean kilt$callScreenMouseReleaseEvents(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        var value = ForgeHooksClient.onScreenMouseReleasedPre(instance, mouseX, mouseY, button);

        if (!value) {
            value = original.call(instance, mouseX, mouseY, button);
            value = ForgeHooksClient.onScreenMouseReleasedPost(instance, mouseX, mouseY, button, value);
        }

        return value;
    }

    @Inject(method = "onPress", at = @At("TAIL"))
    private void kilt$onMouseButtonPost(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            ClientHooks.onMouseButtonPost(button, action, modifiers);
        }
    }

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z"))
    private boolean kilt$callScreenMouseScrollEvents(Screen instance, double mouseX, double mouseY, double amount, Operation<Boolean> original, @Cancellable CallbackInfo ci) {
        if (ForgeHooksClient.onScreenMouseScrollPre((MouseHandler) (Object) this, instance, amount)) {
            ci.cancel();
            return true;
        }

        if (original.call(instance, mouseX, mouseY, amount)) {
            ci.cancel();
            return true;
        }

        ForgeHooksClient.onScreenMouseScrollPost((MouseHandler) (Object) this, instance, amount);

        return false;
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void kilt$callForgeMouseScrollEvent(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, @Local(ordinal = 2) double amount) {
        if (ForgeHooksClient.onMouseScroll((MouseHandler) (Object) this, amount))
            ci.cancel();
    }

    @WrapOperation(method = "method_1602", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(DDIDD)Z"))
    private boolean kilt$MouseDragEvents(Screen instance, double mouseX, double mouseY, int button, double dragX, double dragY, Operation<Boolean> original) {
        if (ClientHooks.onScreenMouseDragPre(instance, mouseX, mouseY, button, dragX, dragY)) return true;
        if (original.call(instance, mouseX, mouseY, button, dragX, dragY)) return true;
        ClientHooks.onScreenMouseDragPost(instance, mouseX, mouseY, button, dragX, dragY);
        return false;
    }
}
