// TRACKED HASH: 732ba9ad75d819b941cd1599e61dbe66dbdce285
package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayInject {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "getSystemInformation", at = @At("RETURN"), cancellable = true)
    public void kilt$appendEntityTags(CallbackInfoReturnable<List<String>> cir) {
        if (this.minecraft.showOnlyReducedInfo())
            return;

        var entity = this.minecraft.crosshairPickEntity;

        if (entity != null) {
            var list = cir.getReturnValue();
            entity.getType().builtInRegistryHolder().tags().forEach(t -> list.add("#" + t.location()));

            cir.setReturnValue(list);
        }
    }
}