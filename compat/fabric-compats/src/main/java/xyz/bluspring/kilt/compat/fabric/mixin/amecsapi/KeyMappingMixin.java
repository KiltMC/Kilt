package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.fabric.amecs.KeyMappingWorkaround;

@Pseudo
@IfModLoaded(value = "amecsapi", maxVersion = "1.7.0")
@Mixin(value = KeyMapping.class, priority = 1010)
public abstract class KeyMappingMixin implements IKeyBinding, KeyMappingWorkaround {

    @Shadow
    KeyModifier keyModifier;

    @Shadow
    KeyModifier keyModifierDefault;

    @Override
    public void kilt$amecs$setKeyModifier(KeyModifier keyModifier) {
        switch (keyModifier) {
            case ALT -> amecs$getKeyModifiers().setAlt(true);
            case SHIFT -> amecs$getKeyModifiers().setShift(true);
            case CONTROL -> amecs$getKeyModifiers().setControl(true);
        }
        this.keyModifier = KeyModifier.NONE;
    }

    @Inject(method = "setKeyModifierAndCode", at = @At("HEAD"), cancellable = true)
    private void kilt$amecs$setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Key keyCode, CallbackInfo ci) {
        kilt$amecs$setKeyModifier(keyModifier);
        ci.cancel();
    }

    @Inject(method = "<init>(Ljava/lang/String;Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;Lnet/neoforged/neoforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Key;Ljava/lang/String;)V", at = @At("RETURN"))
    private void kilt$amecs$initWithModifier(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category, CallbackInfo ci) {
        kilt$amecs$setKeyModifier(keyModifier);
    }

    @TargetHandler(
        mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding",
        name = "isDefault"
    )
    @WrapOperation(
        method = "@MixinSquared:Handler",
        at = @At(
            value = "INVOKE",
            target = "Lde/siphalor/amecs/api/KeyModifiers;isUnset()Z"
        )
    )
    private boolean kilt$amecs$isDefault(KeyModifiers instance, Operation<Boolean> original) {
        if (keyModifierDefault != KeyModifier.NONE && keyModifierDefault != null) {
            boolean alt = false;
            boolean ctrl = false;
            boolean shift = false;
            switch (keyModifierDefault) {
                case CONTROL -> ctrl = true;
                case SHIFT -> shift = true;
                case ALT -> alt = true;
            }
            return instance.getAlt() == alt && instance.getShift() == shift && instance.getControl() == ctrl;
        }
        return original.call(instance);
    }

    // Target methods are injected into KeyMapping, hence KeyMapping is the owner, not KeyMappingInject.
    @Definition(id = "getKeyModifier", method = "Lnet/minecraft/client/KeyMapping;getKeyModifier()Lnet/neoforged/neoforge/client/settings/KeyModifier;")
    @Definition(id = "getDefaultKeyModifier", method = "Lnet/minecraft/client/KeyMapping;getDefaultKeyModifier()Lnet/neoforged/neoforge/client/settings/KeyModifier;")
    @Expression("this.getKeyModifier() == this.getDefaultKeyModifier()")
    @TargetHandler(
        mixin = "xyz.bluspring.kilt.injects.client.KeyMappingInject",
        name = "kilt$addModifierToDefaultCheck",
        prefix = "modifyReturnValue"
    )
    @ModifyExpressionValue(
        method = "@MixinSquared:Handler",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private boolean kilt$ameces$disableNeoForgeDefaultCheck(boolean original) {
        return true;
    }

}
