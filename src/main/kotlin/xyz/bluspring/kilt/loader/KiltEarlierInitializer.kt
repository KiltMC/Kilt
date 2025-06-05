package xyz.bluspring.kilt.loader

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import net.fabricmc.loader.api.FabricLoader

class KiltEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        // Force set assertions state in development mode.
        // This is useful for mods that have broken assertions, because otherwise I cannot properly
        // test Kilt in debug mode.
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            this::class.java.classLoader.setDefaultAssertionStatus(KiltFlags.ENABLE_ASSERTIONS)
        }
    }
}