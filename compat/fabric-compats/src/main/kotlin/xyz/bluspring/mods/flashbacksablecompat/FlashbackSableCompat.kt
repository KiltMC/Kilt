package xyz.bluspring.mods.flashbacksablecompat

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation

class FlashbackSableCompat : ModInitializer {
    override fun onInitialize() {
    }

    companion object {
        const val MOD_ID = "flashback_sable"

        @JvmStatic
        fun id(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }
}
