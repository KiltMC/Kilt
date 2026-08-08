package xyz.bluspring.mods.flashbacksablecompat.client

import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.mods.flashbacksablecompat.ModSupport
import xyz.bluspring.mods.flashbacksablecompat.compat.SableSupport

class FlashbackSableCompatClient : ClientModInitializer {
    override fun onInitializeClient() {
        if (ModSupport.SABLE_LOADED && ModSupport.FLASHBACK_LOADED) {
            SableSupport.initialize()
        }
    }
}
