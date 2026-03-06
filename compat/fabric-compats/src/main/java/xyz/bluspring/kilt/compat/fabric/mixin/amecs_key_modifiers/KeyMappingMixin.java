package xyz.bluspring.kilt.compat.fabric.mixin.amecs_key_modifiers;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("amecs_key_modifiers")
@Mixin(value = KeyMapping.class, priority = 1010)
public abstract class KeyMappingMixin {
    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "amecs$getBoundKeyModifiers")
    @Inject(method = "@MixinSquared:Handler", at = @At("RETURN"))
    private void kilt$amecs_key_modifiers$handleForgeModifiers(CallbackInfoReturnable<AmecsKeyModifierCombination> cir) {
        var keyModifiers = cir.getReturnValue();

        var forgeModifier = ((IForgeKeyMapping) this).getKeyModifier();
        if (forgeModifier != KeyModifier.NONE) {
            keyModifiers.unset();

            switch (forgeModifier) {
                case CONTROL -> keyModifiers.setControl(true);
                case SHIFT -> keyModifiers.setShift(true);
                case ALT -> keyModifiers.setAlt(true);
            }
        }
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "getLocalizedName", prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$amecs_key_modifiers$avoidHandlingLocalizedName(CallbackInfo ci) {
        ci.cancel();
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "onKeyPressed", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecs_key_modifiers$avoidCallbackCancel(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "setKeyPressed", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecs_key_modifiers$avoidCallbackCancel2(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "updatePressedStates", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecs_key_modifiers$avoidCallbackCancel3(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "updateKeysByCode", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecs_key_modifiers$avoidCallbackCancel4(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.key_modifiers.impl.mixin.MixinKeyMapping", name = "unpressAll", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecs_key_modifiers$avoidCallbackCancel5(CallbackInfo instance) {
        return false;
    }
}
