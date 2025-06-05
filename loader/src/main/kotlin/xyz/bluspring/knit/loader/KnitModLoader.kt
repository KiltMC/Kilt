package xyz.bluspring.knit.loader

import xyz.bluspring.knit.loader.mod.KnitMod
import xyz.bluspring.knit.loader.mod.ModDefinition
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * An abstracted mod loader system to help load mods into the native (parent) mod loader.
 */
abstract class KnitModLoader<T : KnitMod>(
    /**
     * The ID of the provided mod loader, for example "kilt".
     * This should match with the mod ID, if possible.
     */
    val id: String,

    /**
     * The display name of the supported mod loader, for example "Forge".
     * This will be used for displaying the bridged loader name in the crash reports and other things.
     */
    val supportedLoader: String
) {
    internal val mutableMods: MutableList<T> = mutableListOf()

    /**
     * A list of mods that have been defined by this mod loader.
     */
    val mods: List<T>
        get() = mutableMods

    /**
     * Gets a mod by this ID from this loader.
     */
    fun getModById(id: String): T? {
        return mutableMods.firstOrNull { it.definition.id == id }
    }

    /**
     * The loading priority of mods provided by this loader.
     * The native mod loader will always have their mods at the highest priority, even if the provided version is lower than the
     * one provided by the mod loader, as it is never a guarantee that Knit can override the natively loaded mod, and the native mod
     * will usually have better compatibility with the native loader.
     *
     * In other words, the priority of mod loading follows this order:
     * - If the mod version is higher:
     *   - If also provided by the native loader: Skip
     *   - If another loader provides the mod: Compare loading priority and mod versions
     * - Else: Compare loading priority and mod versions
     *
     * When comparing loading priority, the highest number will always win.
     */
    open val loadingPriority = 1000

    /**
     * Relative to the run directory, but can also be absolute if needed.
     */
    open val modDirs: Set<Path> = setOf(Path("mods"))

    /**
     * Gets the mod definitions of the provided path, for Knit to process. If the mod is invalid for the loader, simply return an empty list.
     */
    abstract fun getModDefinitions(path: Path): List<ModDefinition>

    /**
     * Load the built-in mod definitions from the mod loader. In Kilt, this means loading Forge and the test mods.
     * If there are no built-in mod definitions, just return an empty list.
     */
    open fun getBuiltinModDefinitions(): List<ModDefinition> {
        return emptyList()
    }

    /**
     * Runs after mod scanning has been completed. This is used by Kilt for sorting the mods internally for loading.
     */
    open fun finishModScanning() {}

    /**
     * On Fabric, this runs before mixins get injected into the native mod loader. This is used by Kilt to initialize any important data before mods actually get loaded.
     */
    open fun preInitialize() {}

    /**
     * Creates the containers for each mod after scanning has occurred. Remapping can also occur here.
     * Afterward, every mod provided here will be injected into the native mod loader.
     */
    abstract suspend fun createModContainers(definitions: Collection<ModDefinition>): Collection<T>
}