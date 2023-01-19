package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader

object FMLLoader {
    @JvmStatic
    fun isProduction(): Boolean {
        return !FabricLoader.getInstance().isDevelopmentEnvironment
    }

    @JvmStatic
    fun launcherHandlerName(): String {
        return "kilt"
    }

    @JvmStatic
    fun isSecureJarEnabled(): Boolean {
        return false
    }
}