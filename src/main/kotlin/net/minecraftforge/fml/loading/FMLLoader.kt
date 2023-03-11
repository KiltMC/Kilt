package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

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

    @JvmStatic
    val loadingModList = LoadingModList()

    @JvmStatic
    val gamePath: Path
        get() = FMLPaths.GAMEDIR.get()
}