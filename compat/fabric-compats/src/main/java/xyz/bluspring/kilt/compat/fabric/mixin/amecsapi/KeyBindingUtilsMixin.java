package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.api.KeyBindingUtils;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.fabric.amecs.KeyMappingWorkaround;

@Pseudo
@IfModLoaded(value = "amecsapi", maxVersion = "1.7.0")
@Mixin(value = KeyBindingUtils.class, priority = 1010)
public abstract class KeyBindingUtilsMixin {

    @Inject(
        method = "resetBoundModifiers",
        at = @At("RETURN")
    )
    private static void kilt$amecs$onReset(KeyMapping keyBinding, CallbackInfo ci) {
        var keyModifierDefault = keyBinding.getDefaultKeyModifier();
        if (keyModifierDefault != KeyModifier.NONE) {
            ((KeyMappingWorkaround) keyBinding).kilt$amecs$setKeyModifier(keyModifierDefault);
        }
    }

}
