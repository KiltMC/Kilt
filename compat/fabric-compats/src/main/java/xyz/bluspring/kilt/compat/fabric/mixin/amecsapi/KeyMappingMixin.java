package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("amecsapi")
@Mixin(value = KeyMapping.class, priority = 1010)
public abstract class KeyMappingMixin implements IKeyBinding {
    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "amecs$getKeyModifiers")
    @Inject(method = "@MixinSquared:Handler", at = @At("RETURN"))
    private void kilt$amecsapi$handleForgeModifiers(CallbackInfoReturnable<KeyModifiers> cir) {
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

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "getLocalizedName", prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$amecsapi$avoidHandlingLocalizedName(CallbackInfo ci) {
        ci.cancel();
    }

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "onKeyPressed", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecsapi$avoidCallbackCancel(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "setKeyPressed", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecsapi$avoidCallbackCancel2(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "updatePressedStates", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecsapi$avoidCallbackCancel3(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "updateKeysByCode", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecsapi$avoidCallbackCancel4(CallbackInfo instance) {
        return false;
    }

    @TargetHandler(mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBinding", name = "unpressAll", prefix = "handler")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V"))
    private static boolean kilt$amecsapi$avoidCallbackCancel5(CallbackInfo instance) {
        return false;
    }
}
