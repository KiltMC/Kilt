package xyz.bluspring.kilt.loader.provider

import cpw.mods.modlauncher.api.ServiceRunner
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.loading.targets.CommonLaunchHandler
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.DistUtil

object KiltLaunchHandler : CommonLaunchHandler() {
    override fun getDist(): Dist = DistUtil.envTypeToDist(FabricLoader.getInstance().environmentType)
    override fun getNaming(): String = "intermediary"
    override fun isProduction(): Boolean = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun getMinecraftPaths(): LocatedPaths? = LocatedPaths(listOf(KiltRemapper.gameFile), { a, b -> true },
        FabricLoader.getInstance().allMods.map { it.rootPaths }.toList(), listOf())

    override fun makeService(
        arguments: Array<out String?>?,
        gameLayer: ModuleLayer?
    ): ServiceRunner? {
        return null
    }

    override fun name(): String = "kilt"
}
