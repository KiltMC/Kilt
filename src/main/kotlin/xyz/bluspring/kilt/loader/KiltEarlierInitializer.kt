package xyz.bluspring.kilt.loader

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import kotlinx.coroutines.runBlocking

class KiltEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        runBlocking { KiltLoader.INSTANCE.scanMods() }
    }
}