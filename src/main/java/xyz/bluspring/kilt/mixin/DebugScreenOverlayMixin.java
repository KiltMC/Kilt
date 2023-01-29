package xyz.bluspring.kilt.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(at = @At("RETURN"), method = "getSystemInformation")
    public void kilt$appendModInfo(CallbackInfoReturnable<List<String>> cir) {
        var messages = cir.getReturnValue();

        messages.add("");
        messages.add("Kilt Loader v" + FabricLoader.getInstance().getModContainer("kilt").get().getMetadata().getVersion());
        messages.add(Kilt.Companion.getLoader().getMods().size() + " mods loaded");
        messages.add("");
    }
}
