package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraftforge.fml.ModLoadingPhase
import xyz.bluspring.kilt.Kilt

class KiltEarlyInitializer : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        Kilt.loader.preloadMods()

        Kilt.loader.loadMods()

    }
}