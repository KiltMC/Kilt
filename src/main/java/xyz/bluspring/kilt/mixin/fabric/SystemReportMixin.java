package xyz.bluspring.kilt.mixin.fabric;

import java.util.ArrayList;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.knit.loader.KnitLoader;

import net.minecraft.SystemReport;

import net.fabricmc.loader.api.ModContainer;

@IfModLoaded("fabric-crash-report-info-v1")
@Mixin(value = SystemReport.class, priority = 1050)
public abstract class SystemReportMixin {
    // Knit: Filter our mods from Fabric's list
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.crash.report.info.SystemReportMixin", name = "appendMods")
    @ModifyReceiver(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;"))
    private static ArrayList<ModContainer> knit_loader$removeForgeModsFromList(ArrayList<ModContainer> instance) {
        return new ArrayList<>(instance.stream().filter(e -> !KnitLoader.Companion.getInstance().getContainers().containsValue(e))
                .toList());
    }
}
