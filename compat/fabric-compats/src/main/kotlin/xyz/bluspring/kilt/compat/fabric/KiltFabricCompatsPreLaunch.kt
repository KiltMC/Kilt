package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.loader.DependencyException
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.Constants
import xyz.bluspring.knit.loader.KnitLoader

class KiltFabricCompatsPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        if (Kilt.loader.hasMod("geckolib")) {
            KnitLoader.instance.displayError(Constants.KILT_ERROR_MESSAGE, DependencyException("Detected GeckoLib Forge, please use GeckoLib Fabric instead!"))
            return
        }
    }
}