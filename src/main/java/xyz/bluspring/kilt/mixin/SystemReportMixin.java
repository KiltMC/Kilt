package xyz.bluspring.kilt.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.mod.ForgeMod;
import xyz.bluspring.kilt.loader.mod.fabric.KiltFabricModContainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;

@Mixin(value = SystemReport.class, priority = 1050)
public abstract class SystemReportMixin {
    @Shadow public abstract void setDetail(String identifier, Supplier<String> valueSupplier);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kilt$appendForgeMods(CallbackInfo ci) {
        this.setDetail("Forge Mods (Kilt)", () -> {
            var modString = new StringBuilder();
            var mods = new ArrayList<>(Kilt.Companion.getLoader().getMods());
            mods.sort(Comparator.comparing(ForgeMod::getModId));

            // TODO: Try handling JiJ'd mods?
            for (ForgeMod mod : mods) {
                modString.append('\n');
                modString.append("\t".repeat(2));
                modString.append(mod.getModId());
                modString.append(": ");
                modString.append(mod.getDisplayName());
                modString.append(' ');
                modString.append(mod.getVersion());
            }

            return modString.toString();
        });
    }

    // Kilt: Filter Forge mods from Fabric's list
    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "MixinAnnotationTarget"})
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.crash.report.info.SystemDetailsMixin", name = "appendMods")
    @ModifyReceiver(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;"))
    private static ArrayList<ModContainer> kilt$removeForgeModsFromList(ArrayList<ModContainer> instance) {
        return new ArrayList<>(instance.stream().filter(e -> !(e instanceof KiltFabricModContainer))
            .toList());
    }
}