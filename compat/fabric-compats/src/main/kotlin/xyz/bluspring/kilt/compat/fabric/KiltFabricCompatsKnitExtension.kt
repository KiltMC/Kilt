package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager
import xyz.bluspring.kilt.api.compatibility.ModBridgeStrategy
import xyz.bluspring.kilt.compat.fabric.architectury.KiltArchitecturyApiCompat
import xyz.bluspring.kilt.compat.fabric.automodpack.KiltAutoModpackCompat
import xyz.bluspring.kilt.compat.fabric.geckolib.GeckoLibEvents
import xyz.bluspring.kilt.compat.fabric.sable.SableCompatBridge
import xyz.bluspring.kilt.compat.fabric.veil.VeilCompatBridge
import xyz.bluspring.knit.loader.api.KnitModScanSetupApi
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension
import xyz.bluspring.knit.loader.mod.ModEnvironment

class KiltFabricCompatsKnitExtension : KnitNativeModCompatExtension {
    override fun setupModScanning(api: KnitModScanSetupApi) {
        if (FabricLoader.getInstance().isModLoaded("automodpack")) {
            KiltAutoModpackCompat.modpackDir?.let { path ->
                for (modDir in api.loader.modDirs) {
                    if (modDir.isAbsolute) continue
                    api.addModDirectory(path.resolve(modDir))
                }
            }
        }

        // Funny little workaround to avoid classload issues in mixin later
        if (FabricLoader.getInstance().isModLoaded("sable")) {
            try {
                Class.forName($$"dev.ryanhcode.sable.mixin.AbstractSableMixinPlugin$MixinConstraints")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        /*
        listOf(
            "FreeNativeResources",
            "VeilAddShaderProcessors",
            "VeilDynamicBuffersChanged",
            "VeilPostProcessing",
            "VeilRegisterBlockLayers",
            "VeilRegisterFixedBuffers",
            "VeilRegisterGlobalControllers",
            "VeilRendererAvailable",
            "VeilShaderCompile"
        ).map { "foundry.veil.forge.event.Forge${it}Event" }
         */
        KiltModCompatBridgeManager.register("sable", listOf("sable-neoforge.mixins.json"), strategy = ModBridgeStrategy.PreferFabric) {
            SableCompatBridge.init()
        }

        KiltModCompatBridgeManager.register("veil", strategy = ModBridgeStrategy.PreferFabric) {
            VeilCompatBridge.init()
        }

        KiltModCompatBridgeManager.register("geckolib", strategy = ModBridgeStrategy.PreferFabric, environment = ModEnvironment.CLIENT) {
            GeckoLibEvents.init()
        }

        KiltModCompatBridgeManager.register("architectury", strategy = ModBridgeStrategy.PreferFabric) {
            KiltArchitecturyApiCompat.initCommon()
        }
    }
}
