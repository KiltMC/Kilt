package xyz.bluspring.kilt.compat.create

import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager
import xyz.bluspring.kilt.api.compatibility.ModBridgeStrategy
import xyz.bluspring.kilt.compat.create.flywheel.FlywheelCompatBridge
import xyz.bluspring.knit.loader.api.KnitModScanSetupApi
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension
import xyz.bluspring.knit.loader.mod.ModEnvironment

class KiltCreateCompatKnitExtension : KnitNativeModCompatExtension {
    override fun setupModScanning(api: KnitModScanSetupApi) {
        KiltModCompatBridgeManager.register("flywheel", environment = ModEnvironment.CLIENT, strategy = ModBridgeStrategy.PreferFabric("Detected Flywheel NeoForge, please install the Vanillin mod to use Flywheel Fabric, or install the latest Flywheel Fabric available from https://maven.createmod.net/dev/engine-room/flywheel")) {
            FlywheelCompatBridge.init()
        }
    }
}
