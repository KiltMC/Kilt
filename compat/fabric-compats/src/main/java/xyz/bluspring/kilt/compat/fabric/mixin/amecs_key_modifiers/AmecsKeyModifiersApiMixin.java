package xyz.bluspring.kilt.compat.fabric.mixin.amecs_key_modifiers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.fabric.amecs.KeyMappingWorkaround;

@Pseudo
@IfModLoaded("amecs_key_modifiers")
@Mixin(value = AmecsKeyModifiersApi.class, priority = 1010)
public class AmecsKeyModifiersApiMixin {

    @WrapOperation(
        method = "resetBoundModifiers",
        at = @At(
            value = "INVOKE",
            target = "Lde/siphalor/amecs/key_modifiers/api/AmecsKeyModifierCombination;unset()V"
        )
    )
    private static void kilt$amecs$onReset(
        AmecsKeyModifierCombination instance, Operation<Void> original,
        @Local(argsOnly = true, name = "mapping") KeyMapping mapping
    ) {
        original.call(instance);
        var keyModifierDefault = mapping.getDefaultKeyModifier();
        if (keyModifierDefault != KeyModifier.NONE) {
            ((KeyMappingWorkaround) mapping).kilt$amecs$setKeyModifier(keyModifierDefault);
        }
    }

}
