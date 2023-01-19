package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

class FMLPaths private constructor(private val path: Path) {
    fun get(): Path {
        return path
    }

    fun relative(): Path {
        return path.relativize(gameDir)
    }

    companion object {
        private val gameDir = FabricLoader.getInstance().gameDir.toAbsolutePath()

        @JvmField
        val GAMESDIR = FMLPaths(FabricLoader.getInstance().gameDir)

        @JvmField
        val MODSDIR = FMLPaths(Path.of(gameDir.toString(), "mods"))

        @JvmField
        val CONFIGDIR = FMLPaths(FabricLoader.getInstance().configDir)
    }
}