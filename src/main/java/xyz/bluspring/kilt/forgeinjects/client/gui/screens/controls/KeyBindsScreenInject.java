// TRACKED HASH: df869ad68a72bbc774f9fc9ba2748e7540b906d7
package xyz.bluspring.kilt.forgeinjects.client.gui.screens.controls;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBindsScreen.class)
public class KeyBindsScreenInject {
    @Shadow @Nullable public KeyMapping selectedKey;

    @Redirect(method = "method_38532", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void kilt$resetKeyDirectly(KeyMapping instance, InputConstants.Key key) {
        ((IForgeKeyMapping) instance).setToDefault();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void kilt$setKeyModifierUnknown(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ((IForgeKeyMapping) this.selectedKey).setKeyModifierAndCode(KeyModifier.getActiveModifier(), InputConstants.UNKNOWN);
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = At.Shift.BEFORE, ordinal = 1))
    private void kilt$setKeyModifierToKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ((IForgeKeyMapping) this.selectedKey).setKeyModifierAndCode(KeyModifier.getActiveModifier(), InputConstants.getKey(keyCode, scanCode));
    }

    @WrapWithCondition(method = "keyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/controls/KeyBindsScreen;selectedKey:Lnet/minecraft/client/KeyMapping;", ordinal = 3))
    private boolean kilt$setSelectedKeyNullWhenNotModifier(KeyBindsScreen instance, KeyMapping value) {
        return !KeyModifier.isKeyCodeModifier(((IForgeKeyMapping) this.selectedKey).getKey());
    }
}