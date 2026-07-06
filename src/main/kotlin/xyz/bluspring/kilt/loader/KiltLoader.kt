package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import fish.cichlidmc.tinyjson.TinyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.stream.consumeAsFlow
import kotlinx.coroutines.withContext
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.util.FileSystemUtil
import net.neoforged.bus.api.Event
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.moddiscovery.ModFileInfo
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper
import net.neoforged.fml.loading.modscan.ModClassVisitor
import net.neoforged.fml.loading.toposort.TopologicalSort
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.IModLanguageLoader
import net.neoforged.neoforgespi.language.MavenVersionAdapter
import net.neoforged.neoforgespi.language.ModFileScanData
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.Mixins
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.api.KiltWrappedModContainerEntrypoint
import xyz.bluspring.kilt.api.compatibility.BridgeFailedException
import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager
import xyz.bluspring.kilt.helpers.DetectedGLVersion
import xyz.bluspring.kilt.loader.asm.AccessTransformerLoader
import xyz.bluspring.kilt.loader.asm.EnumExtensionLoader
import xyz.bluspring.kilt.loader.asm.coremod.CoreModLoader
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import xyz.bluspring.kilt.loader.mod.NeoForgeModVersion
import xyz.bluspring.kilt.loader.mod.NeoForgeVersionConstraint
import xyz.bluspring.kilt.loader.mod.fabric.WrappedFabricModContainer
import xyz.bluspring.kilt.loader.provider.NoopLanguageLoader
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.util.buildGraph
import xyz.bluspring.kilt.workarounds.ModifiedCloneWorkaroundLoader
import xyz.bluspring.knit.loader.KnitLoader
import xyz.bluspring.knit.loader.KnitModLoader
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModDependency
import xyz.bluspring.knit.loader.mod.ModEnvironment
import xyz.bluspring.knit.loader.mod.VersionConstraint
import xyz.bluspring.knit.loader.util.*
import java.lang.annotation.ElementType
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.io.path.*

class KiltLoader : KnitModLoader<NeoForgeMod>(Kilt.MOD_ID, "NeoForge") {
    private val tomlParser = TomlParser()
    private val loadedModIds = mutableSetOf<String>()

    private val bridgedModDefinitions = mutableListOf<ModDefinition>()

    var config = KiltLoaderConfig()

    val languageLoaders: Collection<IModLanguageLoader> by lazy {
        ServiceLoader.load(IModLanguageLoader::class.java).toList()
    }

    // At this point, this is a wall of shame for mods that bundle both Forge and Fabric as one JAR, but don't actually
    // use the same mod ID.
    private val SKIPPED_FABRIC_MODS = mapOf(
        // Forge ID -> Fabric ID
        "unloaded_activity" to "unloadedactivity"
    )

    // This over here is a wall of shame for mods that use different mod IDs between their Forge and Fabric variants.
    @get:JvmName("getNeoForgeToFabricMods")
    val NEOFORGE_TO_FABRIC_MODS = mapOf(
        // Forge ID -> Fabric ID
        "cloth_config" to "cloth-config",
        "playeranimator" to "player-animator",
    )

    // Otherwise, Neo mods trying to target local names end up failing miserably,
    // due to NeoForge's mixin version being on 0.15.2, which notably misses this commit in 0.17.0
    // that fixes the actual parameter names - https://github.com/FabricMC/Mixin/commit/41a0eadd1847e03e1405811b3209badc98c597de
    override val fabricMixinCompatibilityVersion: Int = FabricUtil.COMPATIBILITY_0_14_0

    init {
        val loader = FabricLoader.getInstance()

        if (loader.environmentType == EnvType.CLIENT && loader.isModLoaded("sodium")) {
            // If someone wants to fix this, be my guest, drop a PR. Don't send threats of forking Kilt just because you don't like the fact that we don't support Embeddium.
            if (loader.isModLoaded("embeddium") && !KiltFlags.FORCE_ALLOW_BLOCKED_MODS) {
                KnitLoader.instance.displayError(KILT_ERROR_MESSAGE, IllegalStateException("Kilt: You are using Embeddium, which is not supported under Kilt!"))
            }
        }

        // Sanity check for determining if Fabric mods are bundling NeoForge classes for whatever reason
        for (container in loader.allMods) {
            // Ignore ourselves and whatever we know works correctly.
            if (container.metadata.id == "kilt" || container.metadata.id == "forgeconfigapiport" ||
                // If the mod parent is Kilt, then it's probably safe.
                (container.containingMod.isPresent && container.containingMod.orElseThrow().metadata.id == "kilt")
            )
                continue

            val path = container.findPath("net/neoforged")

            if (path.isPresent && path.orElseThrow().isDirectory()) {
                Kilt.logger.warn("Kilt: Fabric mod ${container.metadata.name} (${container.metadata.id}) is likely repackaging NeoForge classes! This may lead to a game crash!")
            }
        }

        this.loadConfig()
    }

    fun loadConfig() {
        if (KiltLoaderConfig.PATH.exists()) {
            try {
                KiltLoaderConfig.CODEC.decode(TinyJson.parse(KiltLoaderConfig.PATH.reader(options = arrayOf(StandardOpenOption.READ))))
                    .ifPresentOrElse({
                        this.config = it
                    }, {
                        Kilt.logger.error("An error occurred while trying to load the Kilt loader config! Error: $it")
                    })
            } catch (e: Throwable) {
                Kilt.logger.error("An error occurred while trying to load the Kilt loader config!", e)
            }
        }
    }

    override fun getModDefinitions(path: Path): List<ModDefinition> {
        if (path.extension != "jar")
            return emptyList()

        // Load all mod definitions. This is recursive, and since we also need to handle JiJ, it's separated into another method.
        return loadModDefinitions(path)
    }

    override fun modExistsNatively(id: String): Boolean {
        // Lie to Knit so we can selectively load stuff from this JAR.
        if (KiltModCompatBridgeManager.canMakeActive(id))
            return false

        return super.modExistsNatively(id)
    }

    override fun getNativeModId(dependencyId: String, nativeLoaderName: String): String {
        if (NEOFORGE_TO_FABRIC_MODS.contains(dependencyId))
            return NEOFORGE_TO_FABRIC_MODS[dependencyId]!!

        val loader = FabricLoader.getInstance()
        if (loader.isModLoaded(dependencyId))
            return dependencyId

        // fun times.
        if (loader.isModLoaded(dependencyId.replace("_", "-")))
            return dependencyId.replace("_", "-")
        else if (loader.isModLoaded(dependencyId.replace("_", "")))
            return dependencyId.replace("_", "")

        return super.getNativeModId(dependencyId, nativeLoaderName)
    }

    private fun loadModDefinitions(path: Path, parents: List<ModDefinition>? = null): List<ModDefinition> {
        val jarFile = JarFile(path.toFile())

        // Prevent users from having both Kilt and Connector at the same time.
        if (jarFile.getEntry("org/sinytra/connector/ConnectorUtil.class") != null) {
            throw Exception("Sinytra Connector was detected! I know I said \"Isn't it reasonable to have both?\", but come on!")
        }

        // Avoid loading JiJ'd MixinExtras, we already provide a modern version of it.
        if (jarFile.getEntry("com/llamalad7/mixinextras/injector/ModifyExpressionValue.class") != null) {
            return emptyList()
        }

        // Try to load manifest from JAR file, because it's required for some stuff in Forge mods.
        val manifest = try {
            jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")).use { Manifest(it) }
        } catch (_: Throwable) {
            null
        }

        val definitions = mutableListOf<ModDefinition>()
        val modsTomlEntry = jarFile.getEntry("META-INF/neoforge.mods.toml")

        // News flash! Apparently, Forge supports loading mods like this. I was not aware of that fact.
        if (manifest != null && parents == null && manifest.mainAttributes.getValue("FMLModType") != null)
            definitions.add(createCustomMod(path, manifest))
        else if (modsTomlEntry == null && parents == null)
        // If no mods.toml even exists, just skip it, unless it's JiJ'd.
            return emptyList()
        else if (modsTomlEntry != null) {
            // Load all mod definitions from the TOML.
            jarFile.getInputStream(modsTomlEntry)
                .use {
                    definitions.addAll(parseModsToml(path, tomlParser.parse(it, Charsets.UTF_8), manifest))
                }
        } else {
            // Load JiJ'd libraries
            definitions.add(createCustomMod(path, manifest))
        }

        val rootDefinitions = parents ?: definitions

        // Then, we also want to load all JiJ'd mods.
        val jarJarMetadata = jarFile.getEntry("META-INF/jarjar/metadata.json")

        if (jarJarMetadata != null) {
            val json = jarFile.getInputStream(jarJarMetadata).use { JsonParser.parseReader(it.reader(Charsets.UTF_8)).asJsonObject }
            val exception = RuntimeException("Failed to load JiJ data in mod ${path.fileName}!")

            // Iterate through the JARs that have been JiJ'd
            for (element in json.getAsJsonArray("jars")) {
                val data = element.asJsonObject
                val filePath = data.get("path").asString

                // If the entry doesn't actually exist, simply ignore.
                val entry = jarFile.getEntry(filePath) ?: continue
                val fileName = filePath.split("/").last()

                val file = jarFile.getInputStream(entry).use { extractedModsDir / "${HashUtils.md5Hash(it)}-$fileName" }

                runCatching {
                    file.createFile()
                    jarFile.getInputStream(entry).use { file.writeBytes(it.readAllBytes()) }
                }.onFailure { throwable ->
                    // Handle files that already exist, so we can just have it dealt with right off the bat.
                    if (throwable !is FileAlreadyExistsException && throwable !is java.nio.file.FileAlreadyExistsException && throwable is Exception) {
                        Kilt.logger.error("Failed to load JiJ'd file: $fileName", throwable)
                        exception.addSuppressed(throwable)
                    }
                }

                // Load through the definitions recursively until all JiJ'd mods have been loaded.
                try {
                    definitions.addAll(loadModDefinitions(file, rootDefinitions))
                } catch (e: Throwable) {
                    Kilt.logger.error("Failed to load JiJ'd file: $fileName", e)
                    exception.addSuppressed(e)
                }
            }

            // If something failed, make sure to throw the exception.
            if (exception.suppressed.isNotEmpty())
                throw exception
        }

        return definitions
    }

    // This is used specifically for JiJ'd mods that don't store mods.toml files.
    private fun createCustomMod(modFile: Path, manifest: Manifest?): ModDefinition {
        return ModDefinition(
            modFile,
            "jij_${modFile.nameWithoutExtension.lowercase().replace(Regex("[^a-zA-Z0-9_-]"), "")}",
            "(Kilt JiJ) ${modFile.nameWithoutExtension}",
            description = "This is a JIJ'd (Jar-in-Jar) mod that doesn't contain a mods.toml file, but has been loaded anyway.",
            version = NeoForgeModVersion(DefaultArtifactVersion("0.0.0")),
            license = "All Rights Reserved",

            additionalData = mapOf(
                "manifest" to manifest,
                "isJiJ" to true,
                "config" to NightConfigWrapper(tomlParser.parse(this::class.java.getResource("/default_mods.toml")))
            ),
            loaderCustomData = mapOf(
                "modmenu" to mapOf(
                    // Hide JiJ'd mods under the library badge.
                    "badges" to listOf("library")
                )
            )
        )
    }

    private fun parseModsToml(path: Path, toml: CommentedConfig, manifest: Manifest?, parentId: String? = null, isBuiltIn: Boolean = false): List<ModDefinition> {
        val fileName = path.fileName
        val modLoader = toml.get<String>("modLoader")

        // We need to check if the mod loader in the TOML is valid. Since we don't properly support ModLauncher or custom FML loading sequences, we need to implement support ourselves.
        if (modLoader != "javafml" && modLoader != "lowcodefml" && modLoader != "kotlinforforge") {
            throw IncompatibleModException("NeoForge mod file $fileName is not a supported FML mod! (got: $modLoader)")
        }

        val loaderVersionRange = MavenVersionAdapter.createFromVersionSpec(toml.get("loaderVersion"))
        when (modLoader) {
            "kotlinforforge" -> {
                if (!loaderVersionRange.containsVersion(Constants.KFF_VERSION)) {
                    throw IncompatibleModException("NeoForge mod file $fileName does not support Kotlin for Forge version ${Constants.KFF_VERSION}! (mod supports versions between [$loaderVersionRange])")
                }
            }

            "javafml", "lowcodefml" -> {
                if (!loaderVersionRange.containsVersion(SUPPORTED_FML_VERSION)) {
                    throw IncompatibleModException("NeoForge mod file $fileName does not support Forge loader version ${SUPPORTED_FML_VERSION}! (mod supports versions between [$loaderVersionRange])")
                }
            }
        }

        val definitions = mutableListOf<ModDefinition>()
        val mainConfig = NightConfigWrapper(toml)

        // Load all mod metadata in the TOML, since Forge allows mods to specify multiple mods in the TOML.
        for (metadata in mainConfig.getConfigList("mods")) {
            val modId = metadata.getConfigElement<String>("modId").orElseThrow {
                Exception("NeoForge mod file $fileName does not contain a mod ID!")
            }

            // ffs, why do we have to do this?
            // mods should really use the same mod ID between their mods >:(
            if (SKIPPED_FABRIC_MODS.contains(modId)) {
                Kilt.logger.warn("Mod ID $modId is a combined mod JAR already existing under ID ${SKIPPED_FABRIC_MODS[modId]}, skipping!")
                continue
            }

            if (this.config.forceDisabledModIds.contains(modId)) {
                Kilt.logger.info("Mod ID $modId was detected to be forcefully disabled in the config, skipping.")
                continue
            }

            val modVersion = NeoForgeModVersion(DefaultArtifactVersion(
                // Forge custom-replaces mod versions with string templates, so we need to handle that.
                metadata.getConfigElement<String>("version").orElse("1")
                    .run {
                        if (this == "\${file.jarVersion}")
                            manifest?.mainAttributes?.getValue("Implementation-Version") ?: "0.0NONE"
                        else if (this == "\${global.neoForgeVersion}")
                            SUPPORTED_NEO_API_VERSION.toString()
                        else if (this == "\${global.mcVersion}")
                            MC_VERSION.friendlyString
                        else this
                    }
            ))

            val dependencies = mutableListOf<ModDependency>()

            // Check all dependencies from the provided mod
            for (neoDep in mainConfig.getConfigList("dependencies", modId)) {
                val depId = neoDep.getConfigElement<String>("modId").orElseThrow {
                    Exception("NeoForge mod file $fileName's dependencies contain a dependency without a mod ID!")
                }
                val versionRange = MavenVersionAdapter.createFromVersionSpec(
                    neoDep.getConfigElement<String>("versionRange")
                        .map {
                            if (depId == "minecraft" && (it.startsWith("[1.21,") || it == "[1.21]"))
                                "[1.21,1.21.2)" // Neo, what the fuck? (https://github.com/neoforged/FancyModLoader/blob/1.21.1/loader/src/main/java/net/neoforged/fml/loading/VersionSupportMatrix.java)
                            else it
                        }
                        .orElse("[0,)") // sure
                )

                dependencies.add(ModDependency(
                    id = depId,
                    type = when (neoDep.getConfigElement<String>("type").orElse("optional").lowercase()) {
                        "required" -> ModDependency.Type.REQUIRED
                        "optional" -> ModDependency.Type.OPTIONAL
                        "discouraged" -> ModDependency.Type.DISCOURAGED
                        "incompatible" -> ModDependency.Type.INCOMPATIBLE

                        else -> ModDependency.Type.OPTIONAL
                    },
                    constraint = NeoForgeVersionConstraint(versionRange),

                    // Forge has sided dependencies. How did we get sided dependencies before sided mods?
                    side = when (neoDep.getConfigElement<String>("side").orElse("BOTH")) {
                        "CLIENT" -> ModEnvironment.CLIENT
                        "SERVER" -> ModEnvironment.SERVER
                        "BOTH" -> ModEnvironment.BOTH
                        else -> throw IllegalArgumentException("Invalid side ${neoDep.getConfigElement<String>("side")} provided while handling Forge mod file $fileName!")
                    },

                    // Knit has no reason to handle ordering, but we do, so we store it into the additional data.
                    additionalData = mapOf(
                        "ordering" to IModInfo.Ordering.valueOf(neoDep.getConfigElement<String>("ordering").orElse("NONE"))
                    )
                ))
            }

            val mixinConfigs = (manifest?.mainAttributes?.getValue("MixinConfigs")?.split(",")
                ?.filter { !it.trim().isBlank() } // why the FUCK is this a possibility.
                ?.map { ModDefinition.MixinConfig(it) }
                ?: emptyList()).toMutableSet()

            for (mixinConfig in mainConfig.getConfigList("mixins")) {
                mixinConfigs.add(ModDefinition.MixinConfig(
                    mixinConfig.getConfigElement<String>("config").orElse(null) ?: continue
                ))
            }

            val dependencyOverrides = config.dependencyOverrides[modId]

            if (dependencyOverrides != null) {
                val modifiedDependencies = mutableMapOf<String, ModDependency>()
                for (dep in dependencies) {
                    modifiedDependencies[dep.id] = dep
                }
                for (dep in dependencyOverrides) {
                    val prevDep = modifiedDependencies[dep.key]
                    val additionalData = prevDep?.additionalData ?: mapOf("ordering" to IModInfo.Ordering.NONE)
                    modifiedDependencies[dep.key] = ModDependency(
                        id = prevDep?.id ?: dep.key,
                        constraint = dep.value.version.map { NeoForgeVersionConstraint(it) as VersionConstraint }.orElse(prevDep?.constraint ?: NeoForgeVersionConstraint(VersionRange.createFromVersionSpec("(,1.0],[1.0,)"))),
                        type = dep.value.type.orElse(prevDep?.type ?: ModDependency.Type.OPTIONAL),
                        side = dep.value.side.orElse(prevDep?.side ?: ModEnvironment.BOTH),
                        additionalData = dep.value.ordering.map {
                            additionalData + ("ordering" to it)
                        }.orElse(additionalData)
                    )
                }
                dependencies.clear()
                dependencies.addAll(modifiedDependencies.values)
            }

            val accessTransformerFiles = mutableSetOf<String>()
            for (accessTransformer in mainConfig.getConfigList("accessTransformers")) {
                accessTransformerFiles.add(accessTransformer.getConfigElement<String>("file").orElse(null) ?: continue)
            }

            val definition = ModDefinition(
                id = modId,
                displayName = metadata.getConfigElement<String>("displayName").orElse(modId),
                description = metadata.getConfigElement<String>("description").orElse("")
                    .replace("\r", ""), // Otherwise, the CR gets rendered weirdly into the newlines.
                authors = try {
                    metadata.getConfigElement<String>("authors").orElse("").split(",")
                } catch (_: ClassCastException) {
                    // this is apparently a possibility that I didn't know about? huh.
                    metadata.getConfigElement<List<String>>("authors").orElse(listOf())
                },
                version = modVersion,
                license = toml.get("license"),

                dependencies = dependencies,
                mixinConfigs = mixinConfigs.toList(),
                path = path,

                // Sets the parent ID of the mod definition
                parentId = parentId,

                icon = metadata.getConfigElement<String>("logoFile").orElse(mainConfig.getConfigElement<String>("logoFile").orElse("")) ?: "",

                // Forge mods handle both, there's no way to define sided mods.
                environment = ModEnvironment.BOTH,

                // If this mod is built-in, make sure to specify it.
                isBuiltin = isBuiltIn,

                additionalData = mapOf(
                    "manifest" to manifest,
                    "config" to mainConfig,
                    "loader" to modLoader,
                    "accessTransformers" to accessTransformerFiles.toList(),
                ),

                loaderCustomData = mapOf(
                    "mcb" to listOf<Map<String, Any>>(
                        // https://syorito-hatsuki.github.io/modmenu-badges-lib/
                        mapOf(
                            "name" to "NeoForge",
                            "labelColor" to argb(255, 255, 255),
                            "outlineColor" to argb(207, 128, 55),
                            "fillColor" to argb(136, 60, 18),
                        )
                    )
                )
            )

            definitions.add(definition)
        }

        return definitions
    }

    private fun argb(r: Int, g: Int, b: Int, a: Int = 255): Int {
        // 0xFF_FF_FF_FF
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    override fun getBuiltinModDefinitions(): List<ModDefinition> {
        return if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            val modsList = mutableListOf<ModDefinition>()

            for (url in this::class.java.classLoader.getResources("META-INF/kilt_neoforge.mods.toml")) {
                val toml = tomlParser.parse(url)
                modsList.addAll(parseModsToml(KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath(), toml, null, isBuiltIn = true))
            }

            // Loads gametests
            for (url in this::class.java.classLoader.getResources("META-INF/neoforge.mods.toml")) {
                if (url.file.contains(".jar") && url.file.contains("!/META-INF/neoforge.mods.toml")) // Prevent Fabric mods with broken Forge metadata from loading in the dev env
                    continue

                val path = Path(url.path.removePrefix("file:/"))

                val toml = tomlParser.parse(url)
                modsList.addAll(parseModsToml(path, toml, null, isBuiltIn = true))
            }

            modsList
        } else {
            val kiltFile = KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath()
            val kiltJar = JarFile(kiltFile.toFile())

            val toml = tomlParser.parse(kiltJar.getInputStream(kiltJar.getJarEntry("META-INF/kilt_neoforge.mods.toml")))

            parseModsToml(kiltFile, toml, null, isBuiltIn = true)
        }
    }

    // Copied from HashMap::hash
    fun hash(key: Any?): Int {
        val h: Int
        return if (key == null) 0 else (key.hashCode().also { h = it }) xor (h ushr 16)
    }

    /**
     * NeoForge mods are sorted based on the order of keys in a hash map.
     * See: https://github.com/neoforged/FancyModLoader/blob/1.21.1/loader/src/main/java/net/neoforged/fml/loading/UniqueModListBuilder.java#L43
     * This won't sort the mods in exactly the same order, but it will be close enough to hopefully resolve most issues caused by mods not explicitly denoting their dependencies.
     * Users can always explicitly override this order with dependency overrides.
     */
    fun getNeoForgeNaturalOrder(): Comparator<NeoForgeMod> {
        return Comparator.comparing { hash(it.modId) }
    }

    override fun finishModScanning() {
        // Validate the compatibility bridges
        val failedException = RuntimeException("Failed to load mod compatibility bridges between Fabric and NeoForge mods!")
        for ((entry, _) in KiltModCompatBridgeManager.entries) {
            // Make sure the environment is valid for these bridges before applying them.
            if (entry.environment != ModEnvironment.BOTH) {
                if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && entry.environment != ModEnvironment.CLIENT)
                    continue

                if (FabricLoader.getInstance().environmentType == EnvType.SERVER && entry.environment != ModEnvironment.SERVER)
                    continue
            }

            try {
                entry.strategy.checkValid(entry.fabricModId, entry.neoForgeModId)
            } catch (e: BridgeFailedException) {
                failedException.addSuppressed(e)
                Kilt.logger.error("Failed to load mod compatibility bridge between Fabric mod ID ${entry.fabricModId} and NeoForge mod ID ${entry.neoForgeModId}!", e)
            }
        }

        if (failedException.suppressed.isNotEmpty()) {
            failedException.stackTrace = arrayOf() // We don't want to overwhelm the user with a stack trace.
            KnitLoader.instance.displayError("Kilt: Failed to load mod compatibility bridges!", failedException)
        }

        val graph = this.mods.buildGraph()
        val sorted = TopologicalSort.topologicalSort(graph, getNeoForgeNaturalOrder())

        // Sort the mods, otherwise stuff breaks.
        val modsRef = this.mods as MutableList<NeoForgeMod>
        modsRef.clear()
        modsRef.addAll(sorted)

        if (this.hasMod("embeddium")) {
            KnitLoader.instance.displayError("Kilt: You are using Embeddium, which is not supported under Kilt!", IllegalStateException())
        } else if (this.hasMod("rubidium")) {
            KnitLoader.instance.displayError("Kilt: You are using Rubidium, which is not supported under Kilt!", IllegalStateException())
        }

        // Scan all mod classes. This needs to be run early, because some Forge mods rely on scan data as early as mixin containers.
        scanModClasses()

        // Load mod access transformers and coremods
        for (mod in mods) {
            loadTransformers(mod)
            EnumExtensionLoader.loadEnumExtension(mod)
//            CoreModLoader.scanAndLoadCoreMods(mod)
        }

        EnumExtensionLoader.applyEnumExtensions()
    }

    override suspend fun createModContainers(definitions: Collection<ModDefinition>): Collection<NeoForgeMod> {
        val remappedModsDir = (kiltCacheDir / "remappedMods").apply {
            runCatching { createDirectories() }
        }

        // Remaps all Forge mods from SRG to Intermediary/Yarn/MojMap
        try {
            KiltRemapper.remapMods(definitions, remappedModsDir)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException("Errors occurred while remapping NeoForge mods!", e)
        }

        val mods = mutableListOf<NeoForgeMod>()

        // Then creates the mod containers for each mod.
        for (definition in definitions) {
            loadedModIds.add(definition.id)

            // Don't add the mods that probably shouldn't exist into here.
            if (KiltModCompatBridgeManager.canMakeActive(definition.id)) {
                // Directly inject into the classpath.
                KnitLoader.instance.injectIntoClasspath(definition.path)
                this.bridgedModDefinitions.add(definition)
                continue
            }

            val config = definition.additionalData["config"] as NightConfigWrapper

            mods.add(NeoForgeMod(definition,
                showAsResourcePack = config.getConfigElement<Boolean>("showAsResourcePack").orElse(false),
                modConfig = config,
                modFile = definition.path.run {
                    if (this.extension == "jar")
                        this.toFile()
                    else null // If this is a built-in, let's not actually bother with it
                },
                // Some JiJ'd mods don't have TOML files, but we need to check if they have the "GAMELIBRARY" attribute,
                // because then that verifies that we need to scan it.
                shouldScan = definition.additionalData["isJiJ"] != true || (definition.additionalData["manifest"] as? Manifest?)?.mainAttributes?.getValue("FMLModType") == "GAMELIBRARY",
                accessTransformers = definition.additionalData["accessTransformers"] as? List<String>? ?: listOf()
            ))
        }

        return mods
    }

    override fun preInitialize() {
        // Load Java coremods, this should be handled before access transformers so we don't mess with access modifier stuff.
        // Although, people probably shouldn't be relying on that.
        CoreModLoader.loadJavaCoreMods()
        ModifiedCloneWorkaroundLoader.load() // Init this after coremods so the post transforms always run after.

        // Load all of the Forge access transformers
        AccessTransformerLoader.runTransformers()

        val rootsField = ModContainerImpl::class.java.getDeclaredField("roots").apply {
            isAccessible = true
        }

        for ((entry, _) in KiltModCompatBridgeManager.entries) {
            if (KiltModCompatBridgeManager.isActive(entry)) {
                // Handle loading compatibility mixins where necessary.
                for (mixinConfig in entry.enabledMixinConfigs) {
                    Mixins.addConfiguration(mixinConfig)
                }

                for (config in Mixins.getConfigs()) {
                    if (entry.enabledMixinConfigs.contains(config.name)) {
                        config.config.decorate(FabricUtil.KEY_MOD_ID, entry.fabricModId)
                        config.config.decorate(FabricUtil.KEY_COMPATIBILITY, FabricUtil.COMPATIBILITY_0_14_0)
                    }
                }

                // Inject the Neo paths into their native Fabric mods.
                val neoDefinition = this.bridgedModDefinitions.first { definition -> definition.id == entry.neoForgeModId }
                val fabricContainer = FabricLoader.getInstance().getModContainer(entry.fabricModId).orElseThrow()

                val paths = fabricContainer.rootPaths.toMutableList()
                paths.add(FileSystemUtil.getJarFileSystem(neoDefinition.path, false).get().rootDirectories.first())

                rootsField.set(fabricContainer, paths)
            }
        }
    }

    fun scanModClasses() {
        Kilt.logger.info("Scanning all NeoForge mod classes...")

        val exception = RuntimeException("Failed to scan NeoForge mod classes in Kilt!")

        runBlocking {
            launch(Dispatchers.Default) {
                // Load Forge scan data immediately, then we can assign it when we need to.
                val forgeScanData = ModFileScanData()

                KiltHelper.getForgeClassNodes().asFlow().collect {
                    val visitor = ModClassVisitor()
                    it.accept(visitor)

                    visitor.buildData(forgeScanData.classes, forgeScanData.annotations)
                }

                // TODO: Need to make sure to group mods together so they load in the correct order from each other
                mods.asFlow().concurrent()
                    .collect { mod ->
                        if (!mod.shouldScan) {
                            return@collect
                        }

                        if (mod.modFile == null) { // This is usually a Forge built-in, we don't have to worry about scanning this.
                            // If it is in fact a built-in, let's assign the scan data.
                            if (mod.definition.isBuiltin) {
                                forgeScanData.addModFileInfo(ModFileInfo(mod))
                                mod.scanData.`kilt$copyFrom`(forgeScanData)
                            }

                            return@collect
                        }

                        mod.scanData.addModFileInfo(ModFileInfo(mod))

                        val classes = ConcurrentHashMap.newKeySet<ModFileScanData.ClassData>()
                        val annotations = ConcurrentHashMap.newKeySet<ModFileScanData.AnnotationData>()

                        // basically emulate how Forge loads stuff
                        mod.jar.stream().consumeAsFlow().concurrent()
                            .filter { it.name.endsWith(".class") }
                            .collect { entry ->
                                val visitor = ModClassVisitor()
                                val classReader = withContext(Dispatchers.IO) { mod.jar.getInputStream(entry) }.use { ClassReader(it) }

                                classReader.accept(visitor, 0)
                                visitor.buildData(classes, annotations)
                            }

                        // This needs to be sorted, otherwise there is a very high possibility of packet desync between client-server,
                        // because for whatever reason Forge uses int packet IDs and MCreator mods don't register packets in one place.
                        mod.scanData.classes.addAll(classes.sortedWith { a, b -> a.clazz.className.compareTo(b.clazz.className) })
                        mod.scanData.annotations.addAll(annotations.sortedWith { a, b -> a.clazz.className.compareTo(b.clazz.className) })
                    }
            }.join()
        }

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            KnitLoader.instance.displayError("Errors occurred while scanning NeoForge mod classes!", exception)
        }
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of NeoForge mods...")

        val exception = RuntimeException("Failed to load NeoForge mods in Kilt!")

        // Initialize any compatibility bridges that have been registered
        KiltModCompatBridgeManager.processLoadedMods()

        // Let's provide any Fabric mods with their wrapped container entrypoints
        for (container in FabricLoader.getInstance().getEntrypointContainers(KiltWrappedModContainerEntrypoint.ENTRYPOINT, KiltWrappedModContainerEntrypoint::class.java)) {
            container.entrypoint.onLoadModContainer(WrappedFabricModContainer.get(container.provider))
        }

        // Create all mod containers
        val languageLoaders = this.languageLoaders

        runBlocking {
            val modsByLoaders = mods.groupBy { mod ->
                languageLoaders.firstOrNull { loader ->
                    loader.name() == mod.loader
                } ?: run {
                    exception.addSuppressed(IllegalArgumentException("No language loader found by ID ${mod.loader}!"))
                    NoopLanguageLoader
                }
            }

            if (exception.suppressed.isNotEmpty()) {
                exception.printStackTrace()
                KnitLoader.instance.displayError("Errors occurred while creating NeoForge mods!", exception)
            }

            modsByLoaders.entries.asFlow()
                .collect { (loader, mods) ->
                    mods.asFlow().concurrent().collect { mod ->
                        mod.container = loader.loadMod(mod, mod.scanData, ModuleLayer.empty())
                    }
                }
        }

        // Actual initializing goes into FML now, yay

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            KnitLoader.instance.displayError("Errors occurred while loading NeoForge mods!", exception)
        }
    }

    private fun loadTransformers(mod: NeoForgeMod) {
        val accessTransformers = mod.accessTransformers

        for (atPath in accessTransformers) {
            val accessTransformer = mod.getFile(atPath)

            if (accessTransformer != null) {
                Kilt.logger.info("Found access transformer for ${mod.modId}")
                AccessTransformerLoader.convertTransformers(accessTransformer.readAllBytes())
            }
        }
    }

    fun getOpenGlVersion(): Pair<Int, Int>? {
        var glMajor = 3
        var glMinor = 2

        for (mod in mods) {
            for (data in mod.scanData.getAnnotatedBy(DetectedGLVersion::class.java, ElementType.TYPE)) {
                val major = data.annotationData["majorVersion"] as Int
                val minor = data.annotationData["minorVersion"] as Int

                if (major > glMajor) {
                    glMajor = major
                } else if (major == glMajor && minor > glMinor) {
                    glMinor = minor
                }
            }
        }

        if (glMajor > 3 || glMinor > 2)
            return glMajor to glMinor

        return null
    }

    fun getOpenGlVersionString(): String {
        val version = this.getOpenGlVersion()

        if (version != null) {
            return "${version.first}.${version.second}"
        }

        return "3.2"
    }

    fun postEvent(ev: Event) {
        mods.forEach {
            it.eventBus.post(ev)
        }
    }

    fun getMod(id: String): NeoForgeMod? {
        return mods.firstOrNull { it != null && it.modId == id }
    }

    fun hasMod(id: String): Boolean {
        return this.loadedModIds.contains(id)
    }

    companion object {
        val instance: KiltLoader
            get() = KnitLoader.instance.getLoaderById("kilt") as KiltLoader

        const val KILT_ERROR_MESSAGE = "Kilt: Failed to start Kilt, please read the exception below!"

        // These constants are to be updated each time we change versions
        val SUPPORTED_FML_VERSION = Constants.NEOFORGE_LOADER_VERSION
        val SUPPORTED_NEO_API_VERSION = Constants.NEOFORGE_API_VERSION
        val MC_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version

        private val MOD_ANNOTATION = Type.getType(Mod::class.java)

        val kiltCacheDir = (FabricLoader.getInstance().gameDir / ".kilt").apply {
            runCatching { this.createDirectories() }
        }
        private val extractedModsDir = (kiltCacheDir / "extractedMods").apply {
            runCatching { this.createDirectories() }
        }
    }
}
