package xyz.bluspring.kilt.compat.curios_trinkets

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.config.ModConfig
import xyz.bluspring.kilt.loader.KiltLoader

class KiltCuriosTrinketsCompat : ModInitializer {
    override fun onInitialize() {
        if (isActive) {
            ForgeConfigRegistry.INSTANCE.register("kilt_curios_trinkets_compat", ModConfig.Type.CLIENT, KiltCTCompatConfig.builder.build())
        }
    }

    companion object {
        val isActive = FabricLoader.getInstance().isModLoaded("trinkets") && KiltLoader.INSTANCE.hasMod("curios") && !FabricLoader.getInstance().isModLoaded("accessories")
    }
}