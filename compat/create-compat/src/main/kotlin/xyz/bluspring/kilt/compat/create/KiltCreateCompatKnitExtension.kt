package xyz.bluspring.kilt.compat.create

import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager
import xyz.bluspring.kilt.compat.create.flywheel.FlywheelCompatBridge
import xyz.bluspring.knit.loader.api.KnitModScanSetupApi
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension

class KiltCreateCompatKnitExtension : KnitNativeModCompatExtension {
    override fun setupModScanning(api: KnitModScanSetupApi) {
        KiltModCompatBridgeManager.register("flywheel") {
            FlywheelCompatBridge.init()
        }
    }
}