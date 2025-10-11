package xyz.bluspring.kilt.mixin.debug;

import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.knit.loader.KnitLoader;
import xyz.bluspring.knit.loader.KnitModLoader;
import xyz.bluspring.knit.loader.mod.KnitMod;

import java.util.Comparator;
import java.util.function.Supplier;

@Mixin(SystemReport.class)
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
}
