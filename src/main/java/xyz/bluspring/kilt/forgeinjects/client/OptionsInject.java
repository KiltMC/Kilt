package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsInject {
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

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;minecraft:Lnet/minecraft/client/Minecraft;"))
    private void kilt$setForgeKeybindProperties(Minecraft minecraft, File gameDirectory, CallbackInfo ci) {
        this.setForgeKeybindProperties();
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
