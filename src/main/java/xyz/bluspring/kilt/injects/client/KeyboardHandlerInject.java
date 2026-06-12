package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerInject {
    @Shadow @Final private Minecraft minecraft;

    @WrapMethod(method = "keyPress")
    private void kilt$onKeyPressEvent(long windowPointer, int key, int scanCode, int action, int modifiers, Operation<Void> original) {
        original.call(windowPointer, key, scanCode, action, modifiers);

        // You would think this could be a TAIL injection, but the mixin export disagrees.
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            ClientHooks.onKeyInput(key, scanCode, action, modifiers);
        }
    }

    @WrapOperation(method = {"method_1458", "method_1473"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"))
    private static boolean kilt$onScreenCharTyped(GuiEventListener instance, char codePoint, int modifiers, Operation<Boolean> original) {
        if (ClientHooks.onScreenCharTypedPre((Screen) instance, codePoint, modifiers))
            return true;
        if (original.call(instance, codePoint, modifiers))
            return true;
        ClientHooks.onScreenCharTypedPost((Screen) instance, codePoint, modifiers);
        return false;
    }
}
