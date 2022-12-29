package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FilenameFilter
import java.util.concurrent.ConcurrentLinkedQueue

class KiltLoader {
    private val mods = mutableListOf<ForgeMod>()
    private val modLoadingQueue = ConcurrentLinkedQueue<File>()

    fun loadMods() {
        val modsDir = File(FabricLoader.getInstance().gameDir.toFile(), "mods")

        if (!modsDir.exists() || !modsDir.isDirectory)
            throw IllegalStateException("Mods directory doesn't exist! ...how did you even get to this point?")

        val modFiles = modsDir.listFiles { _, name -> name.endsWith(".jar") } ?: throw IllegalStateException("Failed to load mod files!")

        modFiles.forEach {

        }
    }
}