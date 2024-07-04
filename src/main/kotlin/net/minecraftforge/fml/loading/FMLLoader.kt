package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.loading.targets.CommonLaunchHandler
import net.minecraftforge.fml.loading.targets.KnotLaunchHandler
import xyz.bluspring.kilt.util.DistUtil
import java.nio.file.Path

object FMLLoader {
    @JvmStatic
    fun getDist(): Dist {
        return DistUtil.envTypeToDist(FabricLoader.getInstance().environmentType)
    }

    @JvmStatic
    fun isProduction(): Boolean {
        return !FabricLoader.getInstance().isDevelopmentEnvironment
    }

    private val handler = KnotLaunchHandler()

    @JvmStatic
    fun getLaunchHandler(): CommonLaunchHandler {
        return this.handler
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
    fun versionInfo(): VersionInfo {
        return VersionInfo
    }

    @JvmStatic
    val gamePath: Path
        get() = FMLPaths.GAMEDIR.get()
}