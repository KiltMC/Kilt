package xyz.bluspring.kilt.compat.fabric.mixin.amecs_key_modifiers;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyModifiersModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyMapping;

@Pseudo
@IfModLoaded(value = "amecs_key_modifiers")
@Mixin(AmecsKeyMappingManagerLayer.class)
public abstract class AmecsKeyMappingManagerLayerMixin {
    @Inject(method = "areExactModifiersPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private static void kilt$amecs_key_modifiers$disableIfNoModifiersAssigned(KeyMapping keyBinding, CallbackInfoReturnable<Boolean> cir) {
        if (AmecsKeyModifiersApi.getBoundModifiers(keyBinding).isUnset() || AmecsKeyModifiersModule.CURRENT_MODIFIERS.isUnset())
            cir.setReturnValue(false);
    }
}
