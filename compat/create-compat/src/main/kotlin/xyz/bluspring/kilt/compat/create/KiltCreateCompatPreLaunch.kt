package xyz.bluspring.kilt.compat.create

import net.fabricmc.api.EnvType
import net.fabricmc.loader.DependencyException
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.knit.loader.KnitLoader

class KiltCreateCompatPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && Kilt.loader.hasMod("flywheel") && FabricLoader.getInstance().getModContainer("flywheel").orElse(null)?.metadata?.type?.lowercase() == "neoforge") {
            val KILT_ERROR_MESSAGE = "Kilt: Failed to start Kilt, please read the exception below!"
            KnitLoader.instance.displayErrorGUI(KILT_ERROR_MESSAGE, DependencyException("Detected Flywheel NeoForge, please use either Create Fabric or install the Vanillin mod to use Flywheel Fabric!"))
            return
        }
    }
}