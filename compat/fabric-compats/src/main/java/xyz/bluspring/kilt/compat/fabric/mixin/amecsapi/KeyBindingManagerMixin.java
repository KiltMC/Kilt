package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@IfModLoaded("amecsapi")
@Mixin(KeyBindingManager.class)
public abstract class KeyBindingManagerMixin {
    @IfModLoaded(value = "amecsapi", minVersion = "1.5.3")
    @Inject(method = "areExactModifiersPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private static void kilt$amecsapi$disableIfNoModifiersAssigned(KeyMapping keyBinding, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindingUtils.getBoundModifiers(keyBinding).isUnset() || AmecsAPI.CURRENT_MODIFIERS.isUnset())
            cir.setReturnValue(false);
    }

    @IfModLoaded(value = "amecsapi", maxVersion = "1.5.1")
    @ModifyExpressionValue(method = "getMatchingKeyBindings", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"), require = 0)
    private static <T> Stream<T> kilt$amecsapi$removeIfNoModifiersAssigned(Stream<T> original) {
        return original.filter(keyBinding -> !KeyBindingUtils.getBoundModifiers((KeyMapping) keyBinding).isUnset() && !AmecsAPI.CURRENT_MODIFIERS.isUnset());
    }
}
