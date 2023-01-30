package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import java.io.File
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
        val GAMEDIR = FMLPaths(FabricLoader.getInstance().gameDir)

        @JvmField
        val MODSDIR = FMLPaths(Path.of(gameDir.toString(), "mods"))

        @JvmField
        val CONFIGDIR = FMLPaths(FabricLoader.getInstance().configDir)

        @JvmStatic
        fun getOrCreateGameRelativePath(path: Path, name: String): Path {
            return File(GAMEDIR.get().resolve(path).toFile(), name).apply {
                if (!this.exists())
                    this.mkdirs()
            }.toPath()
        }
    }
}