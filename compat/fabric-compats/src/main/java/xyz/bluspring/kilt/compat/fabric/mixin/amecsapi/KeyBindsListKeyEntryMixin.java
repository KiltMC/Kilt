package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.bawnorton.mixinsquared.TargetHandler;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.fabric.amecs.KeyMappingWorkaround;

@Pseudo
@IfModLoaded(value = "amecsapi", maxVersion = "1.7.0")
@Mixin(value = KeyBindsList.KeyEntry.class, priority = 1010)
public class KeyBindsListKeyEntryMixin {

    @Shadow
    @Final
    private KeyMapping key;

    @TargetHandler(
        mixin = "de.siphalor.amecs.impl.mixin.MixinKeyBindingEntry",
        name = "onResetButtonClicked"
    )
    @Inject(
        method = "@MixinSquared:Handler",
        at = @At("RETURN")
    )
    private void kilt$amecs$onReset(KeyMapping keyBinding, Button buttonWidget, CallbackInfo callbackInfo, CallbackInfo ci) {
        var keyModifierDefault = key.getDefaultKeyModifier();
        if (keyModifierDefault != KeyModifier.NONE) {
            ((KeyMappingWorkaround) key).kilt$amecs$setKeyModifier(keyModifierDefault);
        }
    }

}
