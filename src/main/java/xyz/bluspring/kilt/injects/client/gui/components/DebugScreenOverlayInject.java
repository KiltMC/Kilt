// TRACKED HASH: 732ba9ad75d819b941cd1599e61dbe66dbdce285
package xyz.bluspring.kilt.injects.client.gui.components;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
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

    // Not a Forge patch, but since we're not properly using ForgeGui we're doing this anyway
    /*@Inject(method = "method_51746", at = @At("HEAD")) // TODO: still needed?
    private void kilt$setupBlendState(GuiGraphics guiGraphics, CallbackInfo ci) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }*/

    // Yes, I'm well aware that these two would be running twice, but it's our only way of actually handling this properly.

    @ModifyArg(method = "drawGameInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"))
    private List<String> kilt$appendForgeDebugTextLinesLeft(List<String> original, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        var event = new CustomizeGuiOverlayEvent.DebugText(this.minecraft.getWindow(), guiGraphics, this.minecraft.getTimer(), new ArrayList<>(original), new ArrayList<>());
        NeoForge.EVENT_BUS.post(event);
        return event.getLeft();
    }

    @ModifyArg(method = "drawSystemInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"))
    private List<String> kilt$appendForgeDebugTextLinesRight(List<String> original, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        var event = new CustomizeGuiOverlayEvent.DebugText(this.minecraft.getWindow(), guiGraphics, this.minecraft.getTimer(), new ArrayList<>(), new ArrayList<>(original));
        NeoForge.EVENT_BUS.post(event);
        return event.getRight();
    }
}