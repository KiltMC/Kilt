package xyz.bluspring.knit.loader.fabric.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.knit.loader.KnitLoader;
import xyz.bluspring.knit.loader.KnitModLoader;
import xyz.bluspring.knit.loader.fabric.KnitLoaderFabric;
import xyz.bluspring.knit.loader.mod.KnitMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;

@Mixin(value = SystemReport.class, priority = 1050)
public abstract class SystemReportMixin {
    @Shadow
    public abstract void setDetail(String identifier, Supplier<String> valueSupplier);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void knit_loader$appendForgeMods(CallbackInfo ci) {
        var loaders = KnitLoader.Companion.getInstance().getLoaders()
            .stream()
            .sorted(Comparator.comparing(KnitModLoader::getId))
            .toList();

        for (KnitModLoader<?> loader : loaders) {
            this.setDetail(loader.getSupportedLoader() + " Mods (" + loader.getId() + ")", () -> {
                var modString = new StringBuilder();
                var mods = loader.getMods().stream().sorted(Comparator.comparing(e -> e.getDefinition().getId())).toList();

                for (KnitMod mod : mods) {
                    modString.append('\n');
                    modString.append("\t".repeat(2));
                    modString.append(mod.getDefinition().getId());
                    modString.append(": ");
                    modString.append(mod.getDefinition().getDisplayName());
                    modString.append(' ');
                    modString.append(mod.getDefinition().getVersion());
                }

                return modString.toString();
            });
        }
    }

    // Kilt: Filter Forge mods from Fabric's list
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.crash.report.info.SystemDetailsMixin", name = "appendMods")
    @ModifyReceiver(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;"))
    private static ArrayList<ModContainer> knit_loader$removeForgeModsFromList(ArrayList<ModContainer> instance) {
        return new ArrayList<>(instance.stream().filter(e -> ((KnitLoaderFabric) KnitLoader.Companion.getInstance()).getContainers().containsValue(e))
            .toList());
    }
}
