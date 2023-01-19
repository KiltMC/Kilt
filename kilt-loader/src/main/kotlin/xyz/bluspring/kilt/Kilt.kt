package xyz.bluspring.kilt

import net.fabricmc.api.ModInitializer
import net.minecraftforge.eventbus.api.BusBuilder
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
        val MOD_ID = "kilt"

        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader = KiltLoader()
        @JvmField val eventBus = BusBuilder.builder().startShutdown().build();
    }
}