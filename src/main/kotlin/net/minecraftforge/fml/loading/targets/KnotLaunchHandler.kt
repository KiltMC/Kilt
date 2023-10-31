package net.minecraftforge.fml.loading.targets

import cpw.mods.modlauncher.api.ServiceRunner
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.api.distmarker.Dist
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class KnotLaunchHandler : CommonLaunchHandler() {
    override fun getDist(): Dist {
        return if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
            Dist.CLIENT
        else
            Dist.DEDICATED_SERVER
    }

    override fun getNaming(): String {
        return FabricLoader.getInstance().mappingResolver.currentRuntimeNamespace
    }

    private val paths = LocatedPaths(
        KiltRemapper.getGameClassPath().toList(), { _, _ ->
            true
        },
        listOf(), listOf()
    )

    override fun getMinecraftPaths(): LocatedPaths {
        return paths
    }

    override fun name(): String {
        return "knot"
    }

    override fun launchService(arguments: Array<String>, gameLayer: ModuleLayer): ServiceRunner {
        return ServiceRunner.NOOP
    }
}