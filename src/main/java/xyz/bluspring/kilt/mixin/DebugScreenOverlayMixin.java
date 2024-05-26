package xyz.bluspring.kilt.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.asm.CoreModLoader;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(at = @At("RETURN"), method = "getSystemInformation")
    public void kilt$appendModInfo(CallbackInfoReturnable<List<String>> cir) {
        var messages = cir.getReturnValue();

        messages.add("");

        var version = FabricLoader.getInstance().getModContainer("kilt").orElseThrow().getMetadata().getVersion().getFriendlyString();
        var color = "§";

        if (version.endsWith("-local"))
            color += "c";
        else if (version.contains("+build."))
            color += "6";
        else
            color += "b";

        messages.add("Kilt Loader " + color + "v" + version);
        messages.add(Kilt.Companion.getLoader().getMods().size() + " mods loaded");
        messages.add(CoreModLoader.INSTANCE.getLoadedCoreMods().size() + " coremods loaded");
        messages.add("");
    }
}
