package xyz.bluspring.kilt.workarounds

import net.minecraft.core.registries.Registries
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.versions.forge.ForgeVersion
import xyz.bluspring.kilt.injections.client.renderer.SheetsInjection

@Mod.EventBusSubscriber(modid = ForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = [ Dist.CLIENT ])
open class ForgePatternSheetResyncWorkaround {
    @SubscribeEvent
    open fun resyncSheetLayers(ev: RegisterEvent) {
        if (ev.registryKey == Registries.BANNER_PATTERN) {
            SheetsInjection.`kilt$resyncBannerSheetLayers`()
        } else if (ev.registryKey == Registries.DECORATED_POT_PATTERNS) {
            SheetsInjection.`kilt$resyncDecoratedPotSheetLayers`()
        }
    }
}