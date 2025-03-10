package xyz.bluspring.kilt.loader

import com.moulberry.mixinconstraints.MixinConstraints
import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class KiltEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        // Ensure that MixinConstraints actually correctly uses Fabric detection.
        val loaderField = MixinConstraints::class.java.getDeclaredField("loader")
        loaderField.isAccessible = true
        loaderField.set(null, MixinConstraints.Loader.FABRIC)

        // Force set assertions state in development mode.
        // This is useful for mods that have broken assertions, because otherwise I cannot properly
        // test Kilt in debug mode.
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            this::class.java.classLoader.setDefaultAssertionStatus(KiltFlags.ENABLE_ASSERTIONS)
        }

        KiltRemapper.init()
        runBlocking { KiltLoader.INSTANCE.scanMods() }
    }
}