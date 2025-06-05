package xyz.bluspring.knit.loader.fabric

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.knit.loader.KnitLoader

class KnitEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        val loader = KnitLoaderFabric()
        KnitLoader.instance = loader

        runBlocking(Dispatchers.IO) {
            // Scans the mods from the game directory.
            loader.scanMods(FabricLoader.getInstance().gameDir)
        }
    }
}