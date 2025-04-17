package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayInject {
    @Shadow @Final private Minecraft minecraft;

    @ModifyReturnValue(method = "getSystemInformation", at = @At("RETURN"))
    public List<String> kilt$appendEntityTags(List<String> original) {
        if (this.minecraft.showOnlyReducedInfo())
            return original;

        var entity = this.minecraft.crosshairPickEntity;

        if (entity != null) {
            entity.getType().builtInRegistryHolder().tags().forEach(t -> original.add("#" + t.location()));
        }

        return original;
    }
}
