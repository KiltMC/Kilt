// TRACKED HASH: df869ad68a72bbc774f9fc9ba2748e7540b906d7
package xyz.bluspring.kilt.forgeinjects.client.gui.screens.controls;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBindsScreen.class)
@Implements(value = @Interface(iface = ContainerEventHandler.class, prefix = "kilt$i$"))
public abstract class KeyBindsScreenInject extends OptionsSubScreen {
    @Shadow @Nullable public KeyMapping selectedKey;
    @Shadow public long lastKeySelection;
    @Shadow private KeyBindsList keyBindsList;

    public KeyBindsScreenInject(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @WrapOperation(method = "method_38532", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void kilt$resetKeyDirectly(KeyMapping instance, InputConstants.Key key, Operation<Void> original) {
        original.call(instance, key);
        ((IForgeKeyMapping) instance).setToDefault();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void kilt$setKeyModifierUnknown(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ((IForgeKeyMapping) this.selectedKey).setKeyModifierAndCode(null, InputConstants.UNKNOWN);
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = At.Shift.BEFORE, ordinal = 1))
    private void kilt$setKeyModifierToKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ((IForgeKeyMapping) this.selectedKey).setKeyModifierAndCode(null, InputConstants.getKey(keyCode, scanCode));
    }

    @WrapWithCondition(method = "keyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/controls/KeyBindsScreen;selectedKey:Lnet/minecraft/client/KeyMapping;", ordinal = 3))
    private boolean kilt$setSelectedKeyNullWhenNotModifier(KeyBindsScreen instance, KeyMapping value, @Local(argsOnly = true, ordinal = 0) int keyCode) {
        return keyCode == 256 || !KeyModifier.isKeyCodeModifier(((IForgeKeyMapping) this.selectedKey).getKey());
    }

    @Intrinsic
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Intrinsic(displace = true) // Kilt: this still feels like magic to me but oh well
    public boolean kilt$i$keyReleased(int keyCode, int scanCode, int modifiers) {
        // Forge: We wait for a second key above if the first press is a modifier
        // but if they release the modifier then set it explicitly.
        var key = InputConstants.getKey(keyCode, scanCode);
        if (this.selectedKey != null && this.selectedKey.getKey() == key) {
            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            this.keyBindsList.resetMappingAndUpdateButtons();
        }

        return this.keyReleased(keyCode, scanCode, modifiers);
    }
}