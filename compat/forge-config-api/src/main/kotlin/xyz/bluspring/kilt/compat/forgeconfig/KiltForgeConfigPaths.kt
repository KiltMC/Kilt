package xyz.bluspring.kilt.compat.forgeconfig

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigPaths
import fuzs.forgeconfigapiport.impl.config.ForgeConfigApiPortConfig
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import net.minecraftforge.common.ForgeConfig
import net.minecraftforge.fml.loading.FMLConfig
import xyz.bluspring.kilt.compat.forgeconfig.mixin.ServerLifecycleHooksAccessor
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

object KiltForgeConfigPaths : ForgeConfigPaths {
    override fun getClientConfigDirectory(): Path {
        return FabricLoader.getInstance().configDir
    }

    override fun getCommonConfigDirectory(): Path {
        return FabricLoader.getInstance().configDir
    }

    override fun getServerConfigDirectory(server: MinecraftServer): Path {
        if (this.forceGlobalServerConfigs()) {
            return FabricLoader.getInstance().configDir
        }

        val config = server.getWorldPath(ServerLifecycleHooksAccessor.`kilt$getServerConfigLevelResource`())
        if (!config.exists()) {
            config.createDirectories()
        }

        return config
    }

    override fun forceGlobalServerConfigs(): Boolean {
        return ForgeConfigApiPortConfig.INSTANCE.getValue("forceGlobalServerConfigs")
    }

    override fun getDefaultConfigsDirectory(): Path {
        return FabricLoader.getInstance().gameDir.resolve(FMLConfig.defaultConfigPath()).apply {
            if (!this.parent.exists() || !this.parent.isDirectory()) {
                this.createParentDirectories()
            }

            if (!this.exists() || !this.isDirectory()) {
                this.createDirectories()
            }
        }
    }
}