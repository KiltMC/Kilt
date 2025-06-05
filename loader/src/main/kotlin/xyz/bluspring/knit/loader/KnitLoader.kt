package xyz.bluspring.knit.loader

import org.jetbrains.annotations.ApiStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.knit.loader.mod.*
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.walk
import kotlin.system.exitProcess

/**
 * The internal loader of Knit, abstracted away for providing support towards Fabric, Quilt, and any future loaders that wish to be supported.
 * Developers should not need to use this class.
 */
@ApiStatus.Internal
abstract class KnitLoader<C>(val nativeModLoaderName: String) {
    val loaders = sortedSetOf<KnitModLoader<*>>(Comparator.comparing { loader -> loader.loadingPriority })
    val containers = mutableMapOf<KnitMod, C>()

    init {
        for (loader in ServiceLoader.load(KnitModLoader::class.java)) {
            logger.info("Found mod loader ${loader.id} for loader ${loader.supportedLoader}.")
            loaders.add(loader)
        }

        logger.info("Knit Loader initialized under $nativeModLoaderName mod loader.")
    }

    abstract fun isValidEnvironment(env: ModEnvironment): Boolean

    suspend fun scanMods(path: Path) {
        val loadersToDefinitions = Collections.synchronizedMap(mutableMapOf<KnitModLoader<*>, MutableSet<ModDefinition>>())

        // Scans all mods, retrieving their mod definitions.
        for (loader in loaders) {
            for (scanDir in loader.modDirs) {
                path.resolve(scanDir).walk().filter { !it.isDirectory() }.forEach { modPath ->
                    for (loader in loaders) {
                        val definitionsToAdd = loader.getModDefinitions(modPath)

                        synchronized(loadersToDefinitions) {
                            val definitions = loadersToDefinitions.computeIfAbsent(loader) { Collections.synchronizedSet(mutableSetOf()) }

                            synchronized(definitions) {
                                definitions.addAll(definitionsToAdd)
                            }
                        }
                    }
                }
            }
        }

        val definitionsToLoad = mutableMapOf<ModDefinition, KnitModLoader<*>>()

        // First pass, get all mods that are to be loaded by Knit.
        for (modId in loadersToDefinitions.values.flatten().distinctBy { it.id }.map { it.id }) {
            // Skip mod if the mod already exists natively
            if (modExistsNatively(modId))
                continue

            // Get all definitions from other loaders that match this definition
            val definitions = loadersToDefinitions
                .mapNotNull { it.key to (it.value.firstOrNull { d -> d.id == modId } ?: return@mapNotNull null) }
                .filter { isValidEnvironment(it.second.environment) } // Remove definitions that don't match the environment.
                .sortedByDescending { it.second.version }

            // Our definitions list is empty, skip.
            if (definitions.isEmpty())
                continue

            val highestDefinition = definitions.first().second

            // Then, sort by loading priority of loaders.
            val prioritizedDefinition = definitions.filter { it.second.version == highestDefinition.version }
                .maxBy { it.first.loadingPriority }

            // The definitions are then added in for the loader to consider.
            definitionsToLoad[prioritizedDefinition.second] = prioritizedDefinition.first
        }

        // We should also load the built-in mod definitions. This occurs after the definition loading above, because some mods may "provide" the mod in their respective metadata files,
        // so the modExistsNatively check may end up thinking it is available.
        for (loader in loaders) {
            for (definition in loader.getBuiltinModDefinitions()) {
                // If the definition somehow already exists, we need to overwrite it with the built-in mod definition.
                val existingDefinition = definitionsToLoad.keys.firstOrNull { it.id == definition.id }
                if (existingDefinition != null) {
                    val otherLoader = definitionsToLoad[existingDefinition]!!
                    logger.warn("Mod definition for ID ${definition.id} already exists! Overwriting. (existing: ${otherLoader.id}/${otherLoader.supportedLoader}, new: ${loader.id}/${loader.supportedLoader})")
                    definitionsToLoad.remove(existingDefinition)
                }

                definitionsToLoad[definition] = loader
            }
        }

        // Second pass, validate all dependencies
        // This is in a separate method to allow for the Quilt module to override and handle the broken dependencies by itself.
        validateDependencies(definitionsToLoad)

        // Third pass, create containers for all mod definitions.
        // Kilt is also able to use this for mod remapping and sorting, and they will be injected into the native mod loader later.
        for (loader in loaders.sortedBy { it.id }) {
            val loaderDefinitions = definitionsToLoad.filterValues { it == loader }.keys
            val containers = loader.createModContainers(loaderDefinitions)

            for (mod in containers) {
                (loader.mutableMods as MutableList<KnitMod>).add(mod)
            }

            for (definition in loaderDefinitions.sortedBy { it.id }) {
                logger.info("Found ${loader.supportedLoader} mod ${definition.displayName} (${definition.id}) version ${definition.version} (loaded by ${loader.id})")
            }
        }

        // We've finished mod scanning now, so let's notify the mod loaders so they can do whatever they want.
        for (loader in loaders) {
            loader.finishModScanning()
        }
    }

    protected open fun validateDependencies(definitions: Map<ModDefinition, KnitModLoader<*>>) {
        val failedDependencies = mutableListOf<DependencyState>()

        for (definition in definitions.keys) {
            for (dependency in definition.dependencies) {
                // Check if we should actually validate this dependency
                if (isValidEnvironment(dependency.side))
                    continue

                // Check if Dependency ID actually exists
                if (dependency.type.checkIsMissing && !modExistsNatively(dependency.id) && definitions.keys.none { it.id == dependency.id }) {
                    failedDependencies.add(MissingDependencyState(definition, dependency, ModVersion.EMPTY))
                    continue
                }

                // If the mod is built-in, focus on using the built-in definition
                val dependencyVersion = if (modExistsNatively(dependency.id) && definitions.keys.none { it.id == dependency.id && it.isBuiltin })
                    getNativeModVersion(dependency.id)
                else
                    definitions.keys.first { it.id == dependency.id }.version

                // Check if dependency constraints match
                if (dependency.constraint.matches(dependencyVersion.toString())) {
                    // If it is discouraged/incompatible, add it to the "failed dependencies" list.
                    if (dependency.type == ModDependency.Type.DISCOURAGED || dependency.type == ModDependency.Type.INCOMPATIBLE) {
                        failedDependencies.add(DependencyExists(definition, dependency, dependencyVersion))
                    }
                } else {
                    // If the constraints do not match, add it in too.
                    failedDependencies.add(MismatchedDependencyVersionState(definition, dependency, dependencyVersion))
                }

                // If everything passes, we don't have to do anything.
            }
        }

        // If something failed, be sure to throw the error.
        if (failedDependencies.isNotEmpty() && failedDependencies.any { it.dependency.type.shouldExitOnFail }) {
            displayError(failedDependencies)
        }
    }

    protected abstract fun <T : KnitMod> createContainer(mod: T): C

    // Injects mods into the native mod loader. This is in a separate method, because Fabric can only have mods injected
    // at the mixin plugin level, whereas the language provider level iterates through the mod candidates, and as such a CME will occur.
    open fun injectModsToLoader() {
        for (loader in loaders) {
            for (mod in loader.mods) {
                val container = createContainer(mod)
                this.containers[mod] = container
            }
        }
    }

    // Injects all modded mixins into the native mod loader. This may be handled by the native mod loader,
    // so we can intentionally keep in empty in some cases.
    // We should also ensure that the loaders are pre-initialized, before mixins get loaded themselves.
    open fun injectModMixins() {
        for (loader in loaders) {
            loader.preInitialize()
        }
    }

    // Displays both a GUI and CLI error to the user.
    protected open fun displayError(failedDependencies: List<DependencyState>) {
        // This is the default CLI error, and should typically also be called.
        logger.error("Knit Loader has detected some incompatible dependencies!")

        val sortedDependencies = failedDependencies
            .map { it.mod to it }
            .groupBy { it.first }
            .mapValues { it.value.map { b -> b.second } }

        for ((mod, states) in sortedDependencies) {
            logger.error("- ${mod.displayName} (${mod.id} - ${mod.originalPath.fileName})")

            for (state in states) {
                if (state.dependency.type.shouldExitOnFail)
                    logger.error("  !! - $state")
                else
                    logger.warn("   - (optional) $state")
            }
        }

        if (failedDependencies.any { it.dependency.type.shouldExitOnFail })
            exitProcess(1)
    }

    open fun displayError(exception: Exception) {
        exception.printStackTrace()

        throw exception
    }

    /**
     * Checks to see if the mod already exists in the native mod loader. Note that after mods are loaded into the native mod loader, that this will become inaccurate.
     */
    abstract fun modExistsNatively(id: String): Boolean
    abstract fun getNativeModVersion(id: String): ModVersion

    fun getLoaderById(id: String): KnitModLoader<*>? {
        return this.loaders.firstOrNull { it.id == id }
    }

    protected class MissingDependencyState(mod: ModDefinition, dependency: ModDependency, version: ModVersion) : DependencyState(mod, dependency, version) {
        override fun toString(): String {
            return "Missing mod with ID \"${dependency.id}\"! (required version: ${dependency.constraint})"
        }
    }

    protected class MismatchedDependencyVersionState(mod: ModDefinition, dependency: ModDependency, version: ModVersion) : DependencyState(mod, dependency, version) {
        override fun toString(): String {
            return "Incompatible version of mod ID \"${dependency.id}\"! (expected: ${dependency.constraint}, got: $version)"
        }
    }
    protected class DependencyExists(mod: ModDefinition, dependency: ModDependency, version: ModVersion) : DependencyState(mod, dependency, version)
    protected abstract class DependencyState(val mod: ModDefinition, val dependency: ModDependency, val version: ModVersion)

    companion object {
        val logger: Logger = LoggerFactory.getLogger("Knit Loader")

        // The instance of Knit Loader that's being used, usually based on the native mod loader.
        lateinit var instance: KnitLoader<*>
    }
}