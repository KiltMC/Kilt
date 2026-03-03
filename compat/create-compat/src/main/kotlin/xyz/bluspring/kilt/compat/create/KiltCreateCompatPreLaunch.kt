package xyz.bluspring.kilt.compat.create

import net.fabricmc.api.EnvType
import net.fabricmc.loader.DependencyException
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.Constants
import xyz.bluspring.knit.loader.KnitLoader

class KiltCreateCompatPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && Kilt.loader.hasMod("flywheel")) {
            KnitLoader.instance.displayError(Constants.KILT_ERROR_MESSAGE, DependencyException("Detected Flywheel Forge, please use either Create Fabric or Flywheel Fabric via Vanillin!"))
            return
        }
    }
}