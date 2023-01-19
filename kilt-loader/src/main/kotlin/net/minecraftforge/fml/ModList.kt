package net.minecraftforge.fml

import net.minecraftforge.fml.loading.moddiscovery.ModFile
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.IModFile
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltModContainer
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.stream.Stream

class ModList private constructor(private val kiltMods: List<ForgeMod>) {
    val mods: List<IModInfo>
        get() = kiltMods.map { it.forgeSpi }

    val modFiles: List<IModFileInfo>
        get() = kiltMods.map { it.forgeSpi.owningFile } // It's funny how stupid this is

    fun <T : Any> getModObjectById(modid: String): Optional<T> {
        return Optional.ofNullable(kiltMods.firstOrNull { it.modInfo.mod.modId == modid }?.modObject as T)
    }

    fun getModContainerById(modId: String): Optional<out ModContainer> {
        val mod = kiltMods.firstOrNull { it.modInfo.mod.modId == modId } ?: return Optional.empty()

        return Optional.of(KiltModContainer(mod))
    }

    fun getModContainerByObject(obj: Any): Optional<out ModContainer> {
        val mod = kiltMods.firstOrNull { it.modObject == obj } ?: return Optional.empty()

        return Optional.of(mod.container)
    }

    fun isLoaded(modTarget: String): Boolean {
        return kiltMods.any { it.modInfo.mod.modId == modTarget }
    }

    fun size(): Int {
        return kiltMods.size
    }

    val allScanData: List<ModFileScanData>
        get() = kiltMods.map { it.scanData }

    fun getModFileById(modid: String): IModFileInfo? {
        return kiltMods.firstOrNull { it.modInfo.mod.modId == modid }?.forgeSpi?.owningFile
    }

    fun forEachModFile(fileConsumer: Consumer<IModFile>) {
        modFiles.forEach {
            fileConsumer.accept(it.file)
        }
    }

    fun <T> applyForEachModFile(function: java.util.function.Function<IModFile, T>): Stream<T> {
        return modFiles.stream().map { it.file }.map(function)
    }

    fun forEachModContainer(modContainerConsumer: BiConsumer<String, ModContainer>) {
        kiltMods.map { it.container }.forEach {
            modContainerConsumer.accept(it.modId, it)
        }
    }

    fun forEachModInOrder(containerConsumer: Consumer<ModContainer>) {
        // As far as I'm aware, it's already pretty sorted.
        kiltMods.map { it.container }.forEach {
            containerConsumer.accept(it)
        }
    }

    fun <T> applyForEachModContainer(function: java.util.function.Function<ModContainer, T>): Stream<T> {
        return kiltMods.stream().map { it.container }.map(function)
    }

    companion object {
        private lateinit var instance: ModList

        @JvmStatic
        fun of(modFiles: List<ModFile>, sortedList: List<ModInfo>): ModList {
            // let's not care about any of those mod files and use what Kilt's loaded
            instance = ModList(Kilt.loader.mods)
            return instance
        }

        @JvmStatic
        fun get(): ModList {
            if (!this::instance.isInitialized)
                instance = ModList(Kilt.loader.mods)

            return instance
        }
    }
}