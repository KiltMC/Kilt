package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.OptionsInjection;

import java.io.File;
import java.util.function.Function;

@Mixin(Options.class)
public abstract class OptionsInject implements OptionsInjection {
    // Kilt: we don't have much reason to fix this

    @Shadow @Final public KeyMapping keyUp;
    @Shadow @Final public KeyMapping keyDown;
    @Shadow @Final public KeyMapping keyLeft;
    @Shadow @Final public KeyMapping keyRight;
    @Shadow @Final public KeyMapping keyJump;
    @Shadow @Final public KeyMapping keyShift;
    @Shadow @Final public KeyMapping keySprint;
    @Shadow @Final public KeyMapping keyAttack;
    @Shadow @Final public KeyMapping keyChat;
    @Shadow @Final public KeyMapping keyPlayerList;
    @Shadow @Final public KeyMapping keyCommand;
    @Shadow @Final public KeyMapping keyTogglePerspective;
    @Shadow @Final public KeyMapping keySmoothCamera;

    @Shadow
    public abstract void load();

    @Unique private boolean kilt$loadOptionsLimited = false;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;minecraft:Lnet/minecraft/client/Minecraft;"))
    private void kilt$setForgeKeybindProperties(Minecraft minecraft, File gameDirectory, CallbackInfo ci) {
        this.setForgeKeybindProperties();
    }

    @Override
    public void load(boolean limited) {
        this.kilt$loadOptionsLimited = limited;
        this.load();
        this.kilt$loadOptionsLimited = false;
    }

    @Inject(method = "processOptions", at = @At("HEAD"))
    private void kilt$initShouldBeLimited(Options.FieldAccess accessor, CallbackInfo ci, @Share("limited") LocalBooleanRef isLimited) {
        isLimited.set(this.kilt$loadOptionsLimited);
    }

    @WrapWithCondition(
        method = "processOptions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;processDumpedOptions(Lnet/minecraft/client/Options$OptionAccess;)V"
        )
    )
    private boolean kilt$preventDumpedOptionsIfLimited(
        Options instance, @Coerce Object optionAccess, @Share("limited") LocalBooleanRef isLimited
    ) {
        return !isLimited.get();
    }

    @WrapOperation(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Lnet/minecraft/client/OptionInstance;)V"))
    private <T> void kilt$preventRunIfLimited(Options.FieldAccess instance, String s, OptionInstance<T> tOptionInstance, Operation<Void> original, @Share("limited") LocalBooleanRef isLimited) {
        if (isLimited.get()) {
            if (s.equals("menuBackgroundBlurriness")) { // Kilt: Pretty good marker for us to stop using the limited rules
                isLimited.set(false);
            }

            return;
        }

        original.call(instance, s, tOptionInstance);
    }

    @WrapOperation(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;I)I"))
    private int kilt$returnSelfIfLimited(Options.FieldAccess instance, String s, int i, Operation<Integer> original, @Share("limited") LocalBooleanRef isLimited) {
        if (isLimited.get()) {
            return i;
        }

        return original.call(instance, s, i);
    }

    @WrapOperation(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Z)Z"))
    private boolean kilt$returnSelfIfLimited(Options.FieldAccess instance, String s, boolean b, Operation<Boolean> original, @Share("limited") LocalBooleanRef isLimited) {
        if (isLimited.get()) {
            return b;
        }

        return original.call(instance, s, b);
    }

    @WrapOperation(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private String kilt$returnSelfIfLimited(Options.FieldAccess instance, String s, String s2, Operation<String> original, @Share("limited") LocalBooleanRef isLimited) {
        if (isLimited.get()) {
            return s2;
        }

        return original.call(instance, s, s2);
    }

    @WrapOperation(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/Object;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private <T> T kilt$returnSelfIfLimited(Options.FieldAccess instance, String s, T t, Function<String, T> stringTFunction, Function<T, String> tStringFunction, Operation<T> original, @Share("limited") LocalBooleanRef isLimited) {
        if (isLimited.get()) {
            return t;
        }

        return original.call(instance, s, t, stringTFunction, tStringFunction);
    }

    @Inject(method = "processOptions", at = @At("TAIL"))
    private void kilt$callProcessOptions(Options.FieldAccess accessor, CallbackInfo ci) {
        this.processOptionsForge(accessor);
    }

    @Unique
    private void processOptionsForge(Options.FieldAccess processor) {
        // technically here in case someone mixins into here for some reason.
    }

    @ModifyExpressionValue(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;saveString()Ljava/lang/String;"))
    private String kilt$appendModifierToSaveString(String original, @Local KeyMapping keyMapping) {
        if (original.indexOf(':') != -1)
            return original;

        return original + (keyMapping.getKeyModifier() != KeyModifier.NONE ? ":" + keyMapping.getKeyModifier() : "");
    }

    @ModifyArg(method = "processOptions", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;getKey(Ljava/lang/String;)Lcom/mojang/blaze3d/platform/InputConstants$Key;"))
    private String kilt$tryReadModifierFromString(String name, @Local KeyMapping keyMapping) {
        if (name.indexOf(':') != -1) {
            var split = name.split(":");

            keyMapping.setKeyModifierAndCode(KeyModifier.valueFromString(split[1]), InputConstants.getKey(split[0]));
            return split[0];
        }

        return name;
    }

    @Unique
    private void setForgeKeybindProperties() {
        var inGame = KeyConflictContext.IN_GAME;
        keyUp.setKeyConflictContext(inGame);
        keyLeft.setKeyConflictContext(inGame);
        keyDown.setKeyConflictContext(inGame);
        keyRight.setKeyConflictContext(inGame);

        keyJump.setKeyConflictContext(inGame);
        keyShift.setKeyConflictContext(inGame);
        keySprint.setKeyConflictContext(inGame);
        keyAttack.setKeyConflictContext(inGame);

        keyChat.setKeyConflictContext(inGame);
        keyPlayerList.setKeyConflictContext(inGame);
        keyCommand.setKeyConflictContext(inGame);
        keyTogglePerspective.setKeyConflictContext(inGame);
        keySmoothCamera.setKeyConflictContext(inGame);
    }
}
