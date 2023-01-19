package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import xyz.bluspring.kilt.util.DistUtil

object FMLEnvironment {
    @JvmField
    val dist = DistUtil.envTypeToDist(FabricLoader.getInstance().environmentType)
    @JvmField
    val naming = FabricLoaderImpl.INSTANCE.gameProvider.gameName
    @JvmField
    val production = !FabricLoader.getInstance().isDevelopmentEnvironment
    // i never bothered
    @JvmField
    val secureJarsEnabled = false
}