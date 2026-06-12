package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.MouseHandlerInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerInject implements MouseHandlerInjection {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0), cancellable = true)
    private void kilt$onMouseButtonPre(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (ClientHooks.onMouseButtonPre(button, action, modifiers)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "method_1611", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"))
    private static boolean kilt$callScreenMouseClickEvents(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        var value = ClientHooks.onScreenMouseClickedPre(instance, mouseX, mouseY, button);

        if (!value) {
            value = original.call(instance, mouseX, mouseY, button);
            value = ClientHooks.onScreenMouseClickedPost(instance, mouseX, mouseY, button, value);
        }

        return value;
    }

    @WrapOperation(method = "method_1605", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(DDI)Z"))
    private static boolean kilt$callScreenMouseReleaseEvents(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        var value = ClientHooks.onScreenMouseReleasedPre(instance, mouseX, mouseY, button);

        if (!value) {
            value = original.call(instance, mouseX, mouseY, button);
            value = ClientHooks.onScreenMouseReleasedPost(instance, mouseX, mouseY, button, value);
        }

        return value;
    }

    @WrapMethod(method = "onPress")
    private void kilt$onMouseButtonPost(long windowPointer, int button, int action, int modifiers, Operation<Void> original) {
        original.call(windowPointer, button, action, modifiers);

        // You would think this could be a TAIL injection, but the mixin export disagrees.
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            ClientHooks.onMouseButtonPost(button, action, modifiers);
        }
    }

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean kilt$callScreenMouseScrollEvents(Screen instance, double mouseX, double mouseY, double scrollX, double scrollY, Operation<Boolean> original, @Cancellable CallbackInfo ci) {
        if (ClientHooks.onScreenMouseScrollPre((MouseHandler) (Object) this, instance, scrollX, scrollY)) {
            ci.cancel();
            return true;
        }

        if (original.call(instance, mouseX, mouseY, scrollX, scrollY)) {
            ci.cancel();
            return true;
        }

        ClientHooks.onScreenMouseScrollPost((MouseHandler) (Object) this, instance, scrollX, scrollY);

        return false;
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void kilt$callForgeMouseScrollEvent(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, @Local(ordinal = 2) double scrollX, @Local(ordinal = 3) double scrollY) {
        if (ClientHooks.onMouseScroll((MouseHandler) (Object) this, scrollX, scrollY))
            ci.cancel();
    }

    @WrapOperation(method = "method_55795", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(DDIDD)Z"))
    private boolean kilt$MouseDragEvents(Screen instance, double mouseX, double mouseY, int button, double dragX, double dragY, Operation<Boolean> original) {
        if (ClientHooks.onScreenMouseDragPre(instance, mouseX, mouseY, button, dragX, dragY)) return true;
        if (original.call(instance, mouseX, mouseY, button, dragX, dragY)) return true;
        ClientHooks.onScreenMouseDragPost(instance, mouseX, mouseY, button, dragX, dragY);
        return false;
    }

    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private <T> T kilt$getTurnPlayerValuesEvent(T original, @Share("event") LocalRef<CalculatePlayerTurnEvent> eventRef) {
        if (original instanceof Double d) {
            eventRef.set(ClientHooks.getTurnPlayerValues(d, this.minecraft.options.smoothCamera));
            return (T) (Object) eventRef.get().getMouseSensitivity();
        }

        return original;
    }

    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z", opcode = Opcodes.GETFIELD))
    private boolean kilt$checkShouldHaveSmoothCamera(boolean original, @Share("event") LocalRef<CalculatePlayerTurnEvent> eventRef) {
        if (eventRef.get() != null && eventRef.get().kilt$wasModified) {
            return eventRef.get().getCinematicCameraEnabled();
        }

        return original;
    }

    @Override
    public double getXVelocity() {
        return this.accumulatedDX;
    }

    @Override
    public double getYVelocity() {
        return this.accumulatedDY;
    }
}
