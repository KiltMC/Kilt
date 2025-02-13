package xyz.bluspring.kilt.loader

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import kotlinx.coroutines.runBlocking
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class KiltEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        KiltRemapper.init()
        runBlocking { KiltLoader.INSTANCE.scanMods() }
    }
}