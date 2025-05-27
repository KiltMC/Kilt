package xyz.bluspring.knit.loader

import kotlinx.coroutines.flow.asFlow
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import xyz.bluspring.knit.loader.mod.KnitMod
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModDependency
import xyz.bluspring.knit.loader.mod.ModVersion
import xyz.bluspring.knit.loader.util.collect
import xyz.bluspring.knit.loader.util.concurrent
import xyz.bluspring.knit.loader.util.filter
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
abstract class KnitLoader<C> {
    private val logger = LoggerFactory.getLogger("Knit Loader")

    val loaders = sortedSetOf<KnitModLoader<*>>(Comparator.comparing { loader -> loader.loadingPriority })
    val containers = mutableMapOf<KnitMod, C>()

    init {
        for (loader in ServiceLoader.load(KnitModLoader::class.java)) {
            loaders.add(loader)
        }
    }

    suspend fun scanMods(path: Path) {
        val loadersToDefinitions = Collections.synchronizedMap(mutableMapOf<KnitModLoader<*>, MutableSet<ModDefinition>>())

        // Scans all mods, retrieving their mod definitions.
        path.walk().asFlow().concurrent().filter { !it.isDirectory() }.collect { modPath ->
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

        val definitionsToLoad = mutableMapOf<ModDefinition, KnitModLoader<*>>()

        // First pass, get all mods that are to be loaded by Knit.
        for (modId in loadersToDefinitions.values.flatten().distinctBy { it.id }.map { it.id }) {
            // Skip mod if the mod already exists natively
            if (modExistsNatively(modId))
                continue

            // Get all definitions from other loaders that match this definition
            val definitions = loadersToDefinitions
                .mapNotNull { it.key to (it.value.firstOrNull { d -> d.id == modId } ?: return@mapNotNull null) }
                .sortedByDescending { it.second.version }

            val highestDefinition = definitions.first().second

            // Then, sort by loading priority of loaders.
            val prioritizedDefinition = definitions.filter { it.second.version == highestDefinition.version }
                .maxBy { it.first.loadingPriority }

            // The definitions are then added in for the loader to consider.
            definitionsToLoad[prioritizedDefinition.second] = prioritizedDefinition.first
        }

        // Second pass, validate all dependencies
        // This is in a separate method to allow for the Quilt module to override and handle the broken dependencies by itself.
        validateDependencies(definitionsToLoad)
    }

    open fun validateDependencies(definitions: Map<ModDefinition, KnitModLoader<*>>) {
        val failedDependencies = mutableListOf<DependencyState>()

        for (definition in definitions.keys) {
            for (dependency in definition.dependencies) {
                // Check if Dependency ID actually exists
                if (dependency.type.checkIsMissing && !modExistsNatively(dependency.id) && definitions.keys.none { it.id == dependency.id }) {
                    failedDependencies.add(MissingDependencyState(definition, dependency, ModVersion.EMPTY))
                    continue
                }

                val dependencyVersion = if (modExistsNatively(dependency.id))
                    getNativeModVersion(dependency.id)
                else
                    definitions.keys.first { it.id == dependency.id }.version

                // Check if dependency constraints match
                if (dependency.constraint.matches(dependencyVersion.asString)) {
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
        if (failedDependencies.isNotEmpty()) {
            displayError(failedDependencies)
        }
    }

    abstract fun <T : KnitMod> createContainer(mod: T): C

    protected open fun displayError(failedDependencies: List<DependencyState>) {
        logger.warn("Knit Loader has detected some incompatible dependencies!")

        val sortedDependencies = failedDependencies
            .map { it.mod to it }
            .groupBy { it.first }
            .mapValues { it.value.map { b -> b.second } }

        for ((mod, states) in sortedDependencies) {
            logger.error("- ${mod.displayName} (${mod.id})")

            for (state in states) {
                if (state.dependency.type.shouldExitOnFail)
                    logger.error("  - $state")
                else
                    logger.warn("   - $state")
            }
        }

        if (failedDependencies.any { it.dependency.type.shouldExitOnFail })
            exitProcess(1)
    }

    open fun displayError(exception: Exception) {
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
            return "Invalid version for mod ID \"${dependency.id}\"! (expected: ${dependency.constraint}, got: $version)"
        }
    }
    protected class DependencyExists(mod: ModDefinition, dependency: ModDependency, version: ModVersion) : DependencyState(mod, dependency, version)
    protected abstract class DependencyState(val mod: ModDefinition, val dependency: ModDependency, val version: ModVersion)
}