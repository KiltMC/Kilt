package net.minecraftforge.fml.loading

import net.fabricmc.loader.impl.FabricLoaderImpl
import xyz.bluspring.kilt.loader.KiltLoader

object VersionInfo {
    fun mcAndForgeVersion(): String {
        return "${mcVersion()}-${forgeVersion()}"
    }

    fun mcAndMCPVersion(): String {
        return "${mcVersion()}-${mcpVersion()}"
    }

    fun mcpVersion(): String {
        return "intermediary"
    }

    fun forgeVersion(): String {
        return KiltLoader.SUPPORTED_FORGE_API_VERSION.toString()
    }

    fun forgeGroup(): String {
        return "net.minecraftforge"
    }

    fun mcVersion(): String {
        return FabricLoaderImpl.INSTANCE.gameProvider.normalizedGameVersion
    }
}