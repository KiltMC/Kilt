package xyz.bluspring.kilt

import net.fabricmc.api.ModInitializer
import xyz.bluspring.kilt.loader.KiltLoader

class Kilt : ModInitializer {
    override fun onInitialize() {

    }

    companion object {
        val loader: KiltLoader = KiltLoader()
    }
}