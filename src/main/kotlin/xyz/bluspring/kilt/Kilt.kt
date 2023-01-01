package xyz.bluspring.kilt

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltLoader

class Kilt : ModInitializer {
    override fun onInitialize() {
        loader.mods.forEach { mod ->
            mod.eventBus.post(FMLCommonSetupEvent(mod, ModLoadingStage.COMMON_SETUP))
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader = KiltLoader()

        @JvmStatic
        fun superEarlyInit() {
            try {
                loader.preloadMods()
            } catch (e: Exception) {
                FabricGuiEntry.displayError("An error occurred during Kilt super early initialization!", null, {
                    val tab = it.addTab("Kilt Error")

                    tab.node.addCleanedException(e)

                    it.tabs.removeIf { t -> t != tab }
                }, true)
            }
        }
    }
}