// TRACKED HASH: df869ad68a72bbc774f9fc9ba2748e7540b906d7
package xyz.bluspring.kilt.injects.client.gui.screens.options.controls;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(KeyBindsScreen.class)
@Implements(value = @Interface(iface = ContainerEventHandler.class, prefix = "kilt$i$"))
public abstract class KeyBindsScreenInject extends OptionsSubScreen {
    @Shadow @Nullable public KeyMapping selectedKey;

    @Unique private InputConstants.Key lastPressedKey = InputConstants.UNKNOWN;
    @Unique private InputConstants.Key lastPressedModifier = InputConstants.UNKNOWN;
    @Unique private boolean isLastKeyHeldDown = false;
    @Unique private boolean isLastModifierHeldDown = false;

    public KeyBindsScreenInject(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @WrapOperation(method = "method_60342", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void kilt$resetKeyDirectly(KeyMapping instance, InputConstants.Key key, Operation<Void> original) {
        original.call(instance, key);
        ((IKeyMappingExtension) instance).setToDefault();
    }


    @Unique
    private boolean kilt$releasing = false;

    @Unique
    private final Set<InputConstants.Key> kilt$doReleaseLogic = new HashSet<>();

    @Definition(id = "selectedKey", field = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsScreen;selectedKey:Lnet/minecraft/client/KeyMapping;")
    @Expression("this.selectedKey != null")
    @ModifyExpressionValue(
        method = "keyPressed",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private boolean kilt$checkOSX(boolean original, @Local(ordinal = 1, argsOnly = true) int scanCode) {
        if (kilt$releasing) {
            return original && (!Minecraft.ON_OSX || scanCode != 63);
        }
        return original;
    }

    @Definition(id = "keyCode", local = @Local(type = int.class, argsOnly = true, ordinal = 0))
    @Expression("keyCode == 256")
    @Inject(
        method = "keyPressed",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void kilt$checkPassToRelease(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!kilt$releasing) {
            var key = InputConstants.getKey(keyCode, scanCode);
            kilt$doReleaseLogic.add(key);
            if (lastPressedModifier == InputConstants.UNKNOWN && KeyModifier.isKeyCodeModifier(key)) {
                lastPressedModifier = key;
                isLastModifierHeldDown = true;
            } else {
                lastPressedKey = key;
                isLastKeyHeldDown = true;
            }
        }
    }

    @WrapOperation(
        method = "keyPressed",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V",
            ordinal = 0
        )
    )
    private void kilt$setKeyToUnbound(Options options, KeyMapping keyMapping, InputConstants.Key key, Operation<Void> original) {
        if (!kilt$releasing) return;
        this.selectedKey.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.UNKNOWN);
        original.call(options, keyMapping, key);
        lastPressedKey = InputConstants.UNKNOWN;
        lastPressedModifier = InputConstants.UNKNOWN;
        isLastKeyHeldDown = false;
        isLastModifierHeldDown = false;
    }

    @WrapOperation(
        method = "keyPressed",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V",
            ordinal = 1
        )
    )
    private void kilt$setKey(
        Options options, KeyMapping keyMapping, InputConstants.Key key, Operation<Void> original,
        @Cancellable CallbackInfoReturnable<Boolean> cir
    ) {
        if (!kilt$releasing) return;
        if (lastPressedKey.equals(key)) {
            isLastKeyHeldDown = false;
        } else if (lastPressedModifier.equals(key)) {
            isLastModifierHeldDown = false;
        }

        if (!isLastKeyHeldDown && !isLastModifierHeldDown) {
            if (!lastPressedKey.equals(InputConstants.UNKNOWN)) {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.getKeyModifier(lastPressedModifier), lastPressedKey);
                original.call(options, this.selectedKey, lastPressedKey);
            } else {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.NONE, lastPressedModifier);
                original.call(options, this.selectedKey, lastPressedModifier);
            }
            lastPressedKey = InputConstants.UNKNOWN;
            lastPressedModifier = InputConstants.UNKNOWN;
        } else {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "keyPressed",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsScreen;selectedKey:Lnet/minecraft/client/KeyMapping;",
            opcode = Opcodes.PUTFIELD
        ),
        cancellable = true
    )
    private void kilt$afterSetKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!kilt$releasing) cir.setReturnValue(true);
    }

    @Intrinsic
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Intrinsic(displace = true) // Kilt: this still feels like magic to me but oh well
    public boolean kilt$i$keyReleased(int keyCode, int scanCode, int modifiers) {
        var key = InputConstants.getKey(keyCode, scanCode);
        if (kilt$doReleaseLogic.contains(key)) {
            kilt$doReleaseLogic.remove(key);
            kilt$releasing = true;
            boolean result = this.keyPressed(keyCode, scanCode, modifiers);
            kilt$releasing = false;
            return result;
        }
        return this.keyReleased(keyCode, scanCode, modifiers);
    }
}
