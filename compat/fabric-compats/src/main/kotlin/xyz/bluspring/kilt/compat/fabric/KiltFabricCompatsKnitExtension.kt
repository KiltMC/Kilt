package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.compat.fabric.automodpack.KiltAutoModpackCompat
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension
import xyz.bluspring.knit.loader.api.KnitModScanSetupApi

class KiltFabricCompatsKnitExtension : KnitNativeModCompatExtension {

    override fun setupModScanning(api: KnitModScanSetupApi) {
        if (FabricLoader.getInstance().isModLoaded("automodpack")) {
            KiltAutoModpackCompat.getModpackDir()?.let { path ->
                for (modDir in api.loader.modDirs) {
                    if (modDir.isAbsolute) continue
                    api.addModDirectory(path.resolve(modDir))
                }
            }
        }
    }
}