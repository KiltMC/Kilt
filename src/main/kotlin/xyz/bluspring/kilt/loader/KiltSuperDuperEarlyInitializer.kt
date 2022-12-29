package xyz.bluspring.kilt.loader

import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch
import xyz.bluspring.kilt.Kilt

class KiltSuperDuperEarlyInitializer : PrePrePreLaunch {
    override fun onPrePrePreLaunch() {
        Kilt.loader.loadMods()
    }
}