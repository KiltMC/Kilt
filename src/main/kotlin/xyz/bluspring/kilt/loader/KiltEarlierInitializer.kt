package xyz.bluspring.kilt.loader

import com.moulberry.mixinconstraints.MixinConstraints
import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint
import kotlinx.coroutines.runBlocking
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class KiltEarlierInitializer : PrePrePreLaunchEntrypoint {
    override fun onLanguageAdapterLaunch() {
        // Ensure that MixinConstraints actually correctly uses Fabric detection.
        val loaderField = MixinConstraints::class.java.getDeclaredField("loader")
        loaderField.isAccessible = true
        loaderField.set(null, MixinConstraints.Loader.FABRIC)

        KiltRemapper.init()
        runBlocking { KiltLoader.INSTANCE.scanMods() }
    }
}