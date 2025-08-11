// TRACKED HASH: dc2f14e758389a304a6ed1f74286d07572328592
package xyz.bluspring.kilt.injects.client.gui.screens.options.controls;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.class)
public class KeyBindsListInject {
    @Mixin(KeyBindsList.KeyEntry.class)
    public static class KeyEntryInject {
        @Shadow @Final private Button changeButton;

        @Shadow @Final private KeyMapping key;

        @Shadow private boolean hasCollision;

        @Shadow @Final private Button resetButton;

        @Shadow @Final KeyBindsList field_2742;

        @Inject(method = "method_19870", at = @At("HEAD"))
        private void kilt$setKeyToDefault(KeyMapping keyMapping, Button button, CallbackInfo ci) {
            ((IKeyMappingExtension) this.key).setToDefault();
        }

        // Kilt: figured it out!
        @Definition(id = "keyMapping", local = @Local(type = KeyMapping.class))
        @Definition(id = "key", field = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsList$KeyEntry;key:Lnet/minecraft/client/KeyMapping;")
        @Expression("keyMapping != this.key")
        @ModifyExpressionValue(method = "refreshEntry", at = {
            @At("MIXINEXTRAS:EXPRESSION"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;same(Lnet/minecraft/client/KeyMapping;)Z")
        })
        private boolean kilt$checkHasKeyModifierConflict(boolean original, @Local KeyMapping keyMapping) {
            return original || ((IKeyMappingExtension) keyMapping).hasKeyModifierConflict(this.key);
        }
    }
}