package xyz.bluspring.kilt.loader.remap

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.stream.consumeAsFlow
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.MappingResolver
import net.fabricmc.loader.impl.game.GameProviderHelper
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.launch.MappingConfiguration
import net.fabricmc.loader.impl.util.SystemProperties
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedClassRemapper
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.fart.internal.RenamingTransformer
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.INamedMappingFile
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.remap.fixers.*
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinAdditionalRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinShadowRemapper
import xyz.bluspring.kilt.loader.remap.resource.IgnoreSignatureResourceRemapper
import xyz.bluspring.kilt.loader.remap.resource.ManifestResourceRemapper
import xyz.bluspring.kilt.util.CaseInsensitiveStringHashSet
import xyz.bluspring.kilt.util.ClassNameHashSet
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.util.*
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.io.path.*
import kotlin.time.measureTime

object KiltRemapper {
    // Keeps track of the remapper changes, so every time I update the remapper,
    // it remaps all the mods following the remapper changes.
    // this can update by like 12 versions in 1 update, so don't worry too much about it.
    const val REMAPPER_VERSION = 185
    const val MC_MAPPED_JAR_VERSION = 5

    // Kilt JVM flags
    private val forceRemap = KiltFlags.FORCE_REMAPPING
    private val disableRemaps = KiltFlags.DISABLE_REMAPPING
    internal val forceProductionRemap = KiltFlags.FORCE_PRODUCTION_REMAPPING

    // GSON stuff for pretty printing
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    val logConsumer = Consumer<String> {
        logger.debug(it)
    }

    internal val logger = LoggerFactory.getLogger("Kilt Remapper")

    private val launcher = FabricLauncherBase.getLauncher()
    internal val useNamed = launcher.targetNamespace != "intermediary"

    // Remapper extensions
    fun MappingResolver.mapClass(clazz: Class<*>): String = mapClassName("intermediary", "net.minecraft.$clazz").replace(".", "/")

    private val mappingResolver = if (forceProductionRemap)
        NoopMappingResolver()
    else
        FabricLoader.getInstance().mappingResolver

    // This is created automatically using https://github.com/BluSpring/srg2intermediary
    // srg -> intermediary
    val srgIntermediaryMapping: IMappingFile = this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!.buffered().use { IMappingFile.load(it) }
    val fabricMappings: INamedMappingFile = MappingConfiguration::class.java.classLoader.getResourceAsStream("mappings/mappings.tiny")!!.use { INamedMappingFile.load(it) }

    private val devSrgIntermediaryMapping = srgIntermediaryMapping.run {
        if (!forceProductionRemap)
            this.rename(DevMappingRenamer(FabricLoader.getInstance().mappingResolver))
        else
            this
    }

    val intermediarySrgMapping: IMappingFile = srgIntermediaryMapping.reverse()
    private val devIntermediarySrgMapping = devSrgIntermediaryMapping.reverse()

    // if for whatever reason we need these, they're available
    //val srgIntermediaryTree = TinyConverter.convert(srgIntermediaryMapping, "srg", "intermediary")
    //val intermediarySrgTree = TinyConverter.convert(intermediarySrgMapping, "intermediary", "srg")

    // Some workaround mappings, to remap some names to Kilt equivalents.
    // This fixes some compatibility issues.
    private val kiltWorkaroundTree = TinyMappingFactory.load(
        this::class.java.getResourceAsStream("/kilt_workaround_mappings.tiny")!!.bufferedReader()
    )

    private val namespace: String = if (useNamed) launcher.targetNamespace else "intermediary"

    lateinit var enhancedRemapper: KiltEnhancedRemapper

    private lateinit var remappedModsDir: Path

    // SRG name -> (parent class name, intermediary/mapped name)
    val srgMappedFields: Map<String, Pair<String, String>>

    // SRG name -> (parent class name, intermediary/mapped name)
    val srgMappedMethods =
        Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap<String, MutableMap<String, String>>())

    init {
        // Magical field references that are required because otherwise the remapper will deadlock
        val srgIntermediaryMapping = devSrgIntermediaryMapping
        val forceProductionRemap = forceProductionRemap
        val mappingResolver = mappingResolver
        val srgMappedMethods = srgMappedMethods

        srgMappedFields = runBlocking {
            async(Dispatchers.IO) {
                srgIntermediaryMapping.classes.asFlow().concurrent().flatMap {
                    it.fields.asFlow().concurrent().map { f ->
                        f.original to
                                if (!forceProductionRemap)
                                    mappingResolver.mapFieldName(
                                        "intermediary",
                                        it.mapped.replace("/", "."),
                                        f.mapped,
                                        f.mappedDescriptor
                                    )
                                else
                                    f.mapped
                    }.merge(false)
                }.merge(false).toSet().associateBy { it.first }
            }.await()
        }

        runBlocking {
            launch(Dispatchers.IO) {
                srgIntermediaryMapping.classes.asFlow().concurrent().collect {
                    it.methods.asFlow().concurrent().collect { f ->
                        val map = srgMappedMethods.getOrPut(f.original) {
                            Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap())
                        }
                        val mapped = if (!forceProductionRemap)
                            mappingResolver.mapMethodName(
                                "intermediary",
                                it.mapped.replace("/", "."),
                                f.mapped,
                                f.mappedDescriptor
                            )
                        else
                            f.mapped

                        map[f.parent.original] = mapped
                    }
                }
            }.join()
        }
    }

    fun init() {}

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun remapMods(modLoadingQueue: Collection<ModDefinition>, remappedModsDir: Path) {
        if (disableRemaps) {
            logger.warn("Mod remapping has been disabled! Mods built normally using ForgeGradle will not function with this enabled.")
            logger.warn("Only have this enabled if you know what you're doing!")

            return
        }

        this.remappedModsDir = remappedModsDir

        if (forceRemap)
            logger.warn("Forced remaps enabled! All Forge mods will be remapped.")

        // Forcefully initialize the mapping resolver.
        // Otherwise, it just crashes from trying to initialize concurrently.
        mappingResolver.unmapClassName("intermediary", "net.minecraft.class_1937")

        // Automatically delete outdated remapped versions
        run {
            val markedForDeletion = mutableListOf<Path>()

            KiltLoader.kiltCacheDir.walk().forEach {
                if (it.extension != "jar")
                    return@forEach

                if (it.nameWithoutExtension.startsWith("minecraft_") &&
                    (
                        !it.nameWithoutExtension.contains(KiltLoader.MC_VERSION.friendlyString) ||
                        !it.nameWithoutExtension.endsWith("_$MC_MAPPED_JAR_VERSION")
                    )
                ) {
                    markedForDeletion.add(it)
                }
            }

            remappedModsDir.walk().forEach { file ->
                if (file.extension != "jar" && file.extension != "tmp")
                    return@forEach

                val mod = modLoadingQueue.firstOrNull { file.nameWithoutExtension.startsWith(it.id) }

                if (mod == null) {
                    markedForDeletion.add(file)
                    return@forEach
                }

                val fileNameSplit = file.nameWithoutExtension.removePrefix("${mod.id}_").split("_")
                val fileRemapperVersion = fileNameSplit[0].toIntOrNull()
                val fileHash = fileNameSplit[1]

                if (fileRemapperVersion == null) {
                    markedForDeletion.add(file)
                    return@forEach
                }

                if (fileRemapperVersion != REMAPPER_VERSION) {
                    markedForDeletion.add(file)
                    return@forEach
                }

                if (mod.isBuiltin) {
                    markedForDeletion.add(file)
                    return@forEach
                }

                val currentHash = DigestUtils.md5Hex(mod.path.inputStream())

                if (currentHash != fileHash) {
                    markedForDeletion.add(file)
                    return@forEach
                }
            }

            for (path in markedForDeletion) {
                path.deleteIfExists()
            }
        }

        srgGamePath = remapMinecraft("srg", devIntermediarySrgMapping)

        val exception = RuntimeException("Failed to remap Forge mods in Kilt!")

        logger.info("Remapping Forge mods...")

        val mods = modLoadingQueue.filter { !it.isBuiltin }.toSet()

        // Use the regular mod file
        val classProvider = ClassProvider.builder().apply {
            // IMPORTANT: this cannot be a flow or use merge, otherwise the order isn't retained. srgGamePath MUST be at the top of the list.
            listOf(
                srgGamePath,
                // List down Forge paths
                *KiltHelper.getKiltPaths().toTypedArray(),
                // Add all Fabric mods
                *FabricLoader.getInstance().allMods
                    .flatMap { container -> container.rootPaths }.toTypedArray(),
                // add mapped path too
                *getGameClassPath(),
                // Add all Forge mods to the library path, because dependencies don't have to be specified
                // in order to use mods lmao
                *modLoadingQueue.map { mod -> mod.path }.toTypedArray()
            ).forEach {
                addLibrary(it)
            }
        }.build()

        val intermediaryMap = if (FabricLoader.getInstance().isDevelopmentEnvironment && !forceProductionRemap)
            remapMinecraft("intermediary", fabricMappings.getMap("named", "intermediary").rename(DevMojClassMappingRenamer(FabricLoader.getInstance().mappingResolver)))
        else null

        // Initialize a global remapper state
        enhancedRemapper = KiltEnhancedRemapper(classProvider, devSrgIntermediaryMapping, logConsumer) {
            // Initialize this a bit later, cuz we need the same libraries.
            ClassProvider.builder().apply {
                // time to add Intermediary mappings to the mix! :,D
                if (FabricLoader.getInstance().isDevelopmentEnvironment && !forceProductionRemap) {
                    addLibrary(intermediaryMap)
                }

                // IMPORTANT: this cannot be a flow or use merge, otherwise the order isn't retained. srgGamePath MUST be at the top of the list.
                listOf(
                    // List down Forge paths
                    *KiltHelper.getKiltPaths().toTypedArray(),
                    // Add all Fabric mods
                    *FabricLoader.getInstance().allMods
                        .flatMap { container -> container.rootPaths }.toTypedArray(),
                    // add mapped path too
                    *runBlocking { getGameClassPath() },
                    // Add all Forge mods to the library path, because dependencies don't have to be specified
                    // in order to use mods lmao
                    *modLoadingQueue.map { mod -> mod.path }.toTypedArray()
                ).forEach {
                    addLibrary(it)
                }
            }.build()
        }

        //val mixinRemapper = KiltMixinRemapper(enhancedRemapper, srgIntermediaryMapping, classProvider)
        val resourceRemappers = listOf(
            ManifestResourceRemapper,
            IgnoreSignatureResourceRemapper
        )

        if (FabricLoader.getInstance().isDevelopmentEnvironment)
            enhancedRemapper.initDevRemapper()

        suspend fun remapMod(file: Path, mod: ModDefinition) {
            val exception = RuntimeException("Failed to remap Forge mod ${mod.displayName} (${mod.id})!")

            if (mod.isBuiltin) { // Prevent Kilt from remapping *directly* Forge mods. Yes, that started happening.
                return
            }

            // Avoid remapping JiJ'd mods, but also mods like Registrate require remapping, and they don't provide a TOML file.
            if (mod.additionalData["isJiJ"] == true && (mod.additionalData["manifest"] as? Manifest?)?.mainAttributes?.getValue("FMLModType") != "GAMELIBRARY") {
                return
            }

            val hash = withContext(Dispatchers.IO) { DigestUtils.md5Hex(file.inputStream()) }
            val modifiedJarFile = KiltRemapper.remappedModsDir / "${mod.id}_${REMAPPER_VERSION}_$hash.jar"

            if (modifiedJarFile.exists() && !forceRemap) {
                mod.path = modifiedJarFile
                return
            }

            // Make a temporary file, so if any errors occur in remapping, we don't end up with a broken state.
            val tempModifiedJarFile = KiltRemapper.remappedModsDir / "${mod.id}_${REMAPPER_VERSION}_$hash.jar.tmp"
            val jar = withContext(Dispatchers.IO) { JarFile(file.toFile()) }
            val jarOutputStream = withContext(Dispatchers.IO) { JarOutputStream(tempModifiedJarFile.outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) }

            val entryToClassNodes = Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap<JarEntry, ClassNode>())
            val classesToProcess = entryToClassNodes.values.intersect(KiltHelper.getForgeClassNodes().toSet()).toList()

            val mixinClasses = Collections.synchronizedSet(ClassNameHashSet())
            val refmaps = Collections.synchronizedSet(CaseInsensitiveStringHashSet())
            val refmapJsons = Collections.synchronizedMap<JarEntry, JsonObject>(mutableMapOf())

            // JAR validation information stripping.
            // If we can find out how to use this to our advantage prior to remapping,
            // we may still be able to use this information safely.
            val manifestEntry = jar.getJarEntry("META-INF/MANIFEST.MF")
            if (manifestEntry != null) {
                val manifest = jar.getInputStream(manifestEntry).use { Manifest(it) }
                val mixinConfigs = manifest.mainAttributes.getValue("MixinConfigs")?.split(",") ?: listOf()

                // Read mixin configs and add them to the list of mixins to fix
                mixinConfigs.asFlow().concurrent().collect { config ->
                    val jsonEntry = jar.getJarEntry(config) ?: return@collect
                    val json = jar.getInputStream(jsonEntry).use {
                        JsonParser.parseReader(it.reader())
                    }.asJsonObject

                    if (!json.has("package")) return@collect

                    val mixinPackage = json.get("package").asString

                    merge(
                        (json.get("mixins") as? JsonArray)?.asFlow() ?: emptyFlow(),
                        (json.get("client") as? JsonArray)?.asFlow() ?: emptyFlow(),
                        (json.get("server") as? JsonArray)?.asFlow() ?: emptyFlow()
                    ).collect {
                        mixinClasses.add("$mixinPackage.${it.asString}")
                    }

                    runCatching { json.get("refmap")!!.asString }.onSuccess { refmaps.add(it) }
                }
            }

            withContext(Dispatchers.IO) {
                jar.stream().consumeAsFlow().collect { entry ->
                    // Transform some specific files
                    for (remapper in resourceRemappers) {
                        if (remapper.canTransform(entry.name)) {
                            val data = jar.getInputStream(entry).use { remapper.transform(entry.name, it) }

                            if (data != null) {
                                jarOutputStream.putNextEntry(entry)
                                jarOutputStream.write(data)
                                jarOutputStream.closeEntry()
                            }

                            return@collect
                        }
                    }

                    // Store refmap JSONs
                    if (refmaps.contains(entry.name)) {
                        val json = jar.getInputStream(entry).use { it.reader(Charsets.UTF_8).use { r -> JsonParser.parseReader(r) } }

                        if (json.isJsonObject) {
                            refmapJsons[entry] = json.asJsonObject
                        }

                        return@collect
                    }

                    if (entry.name.endsWith(".class")) {
                        // Add the class files for remapping
                        val classReader = jar.getInputStream(entry).use { ClassReader(it) }

                        // we need the info for this for the class writer
                        val classNode = ClassNode(Opcodes.ASM9)
                        classReader.accept(classNode, 0)

                        entryToClassNodes[entry] = classNode
                    } else {
                        // Otherwise, add the entry directly if everything's already been processed.
                        jarOutputStream.putNextEntry(entry)
                        jar.getInputStream(entry).use { it.copyTo(jarOutputStream) }
                        jarOutputStream.closeEntry()
                    }
                }
            }

            val throwable = entryToClassNodes.entries.asFlow().concurrent().runCatching {
                this.collect { (entry, originalNode) ->
                    val remappedNode = ClassNode(Opcodes.ASM9)

                    // only do this on mixin classes, please
                    // We must remap the mixins before actually remapping them to Intermediary, so the names are correct in prod.
                    if (originalNode.name in mixinClasses ||
                        // GUESS WHAT, SOME MODS DON'T FUCKING DEFINE SOME MIXINS IN THE FILE, INSTEAD IN THE MIXIN PLUGIN.
                        // SO LET'S JUST RUN THIS ON EVERYTHING THAT HAS THE BLOODY ANNOTATION.
                        KiltHelper.mergeNullableCollections(originalNode.visibleAnnotations, originalNode.invisibleAnnotations)
                            .any { it.desc == MixinAdditionalRemapper.MIXIN_TYPE.descriptor }
                    ) {
                        MixinRemapper.remapClass(originalNode, enhancedRemapper, refmapJsons.values)
                        MixinShadowRemapper.remapClass(originalNode, enhancedRemapper)
                        MixinAdditionalRemapper.remapClass(originalNode)
                    }

                    originalNode.accept(EnhancedClassRemapper(remappedNode, enhancedRemapper, RenamingTransformer(enhancedRemapper, false)))

                    ConditionalInterfaceInjectionFixer.fixClass(remappedNode)
                    EventClassVisibilityFixer.fixClass(remappedNode)
                    EventEmptyInitializerFixer.fixClass(remappedNode, classesToProcess)
                    InjectedInterfaceVisibilityFixer.fixClass(remappedNode)
                    ObjectHolderDefinalizer.processClass(remappedNode)
                    WorkaroundFixer.fixClass(remappedNode)
                    ConflictingStaticMethodFixer.fixClass(remappedNode)
                    EnvironmentRemapper.remapClass(remappedNode)

                    val writer = ClassWriter(Opcodes.ASM9)
                    remappedNode.accept(writer)

                    synchronized(jarOutputStream) {
                        jarOutputStream.putNextEntry(entry)
                        jarOutputStream.write(writer.toByteArray())
                        jarOutputStream.closeEntry()
                    }
                }
            }.exceptionOrNull()

            if (throwable != null) {
                exception.addSuppressed(throwable)
            }
            
            // If for whatever reason the refmap remapping missed something, we need to remap it immediately.
            MixinRemapper.remapUnmappedRefmaps(refmapJsons.values, enhancedRemapper)

            // Now, let's write the refmap JSONs
            for ((entry, json) in refmapJsons) {
                // First, let's remove the "searge" data map.
                json.getAsJsonObject("data").remove("searge")

                // Now, let's add the mappings data map directly to "named:intermediary"
                json.getAsJsonObject("data").add("named:intermediary", json.getAsJsonObject("mappings"))

                jarOutputStream.putNextEntry(entry)
                jarOutputStream.write(gson.toJson(json).toByteArray(Charsets.UTF_8))
                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            jar.close()

            if (exception.suppressed.isNotEmpty()) {
                throw exception
            } else {
                // We've finished writing the mod file successfully, so let's not make it temporary anymore.
                tempModifiedJarFile.moveTo(modifiedJarFile, true)
            }

            mod.path = modifiedJarFile

            return
        }

        coroutineScope {
            mods.asFlow().concurrent()
                .onEach { mod ->
                    runCatching {
                        logger.info("Remapping ${mod.displayName} (${mod.id})")
                        val ms = measureTime {
                            remapMod(mod.path, mod)
                        }.inWholeMilliseconds
                        logger.info("Remapped ${mod.displayName} (${mod.id}) [took ${ms}ms]")
                    }.onFailure {
                        logger.error("Failed to remap ${mod.displayName} (${mod.id})", it)
                        if (it is Exception) {
                            exception.addSuppressed(it)
                        }
                    }
                }.launchIn(this).join()
        }

        logger.info("Finished remapping mods!")

        if (exception.suppressed.isNotEmpty()) {
            logger.error("Ran into some errors, we're not going to continue with the repairing process.")
            throw exception
        }
    }

    fun remapClass(name: String, toIntermediary: Boolean = false, ignoreWorkaround: Boolean = false): String {
        val workaround = if (!ignoreWorkaround)
            kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt")
        else null
        val intermediary = srgIntermediaryMapping.remapClass(name.replace(".", "/"))
        if (toIntermediary) {
            return workaround ?: intermediary ?: name
        }

        return (workaround ?: if (intermediary != null)
            mappingResolver.mapClassName("intermediary", intermediary.replace("/", ".")) ?: name
        else name).replace(".", "/")
    }

    /**
     * Converts Intermediary (or Mojmap in dev) mappings to SRG mappings.
     */
    fun unmapClass(name: String): String {
        val intermediary = mappingResolver.unmapClassName("intermediary", name.replace("/", "."))
        return intermediarySrgMapping.remapClass(intermediary.replace(".", "/"))
    }

    val gameFile = getMCGameFile()
    lateinit var srgGamePath: Path

    private fun getDeobfJarDir(gameDir: Path, gameId: String, gameVersion: String): Path {
        return GameProviderHelper::class.java
            .getDeclaredMethod("getDeobfJarDir", Path::class.java, String::class.java, String::class.java)
            .apply {
                isAccessible = true
            }
            .invoke(null, gameDir, gameId, gameVersion) as Path
    }

    private fun getMCGameFile(): Path? {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            val commonJar = GameProviderHelper.getCommonGameJar()

            if (commonJar != null)
                return commonJar

            val sidedJar = GameProviderHelper.getEnvGameJar(FabricLoader.getInstance().environmentType)

            if (sidedJar != null)
                return sidedJar

            // this gives the obfuscated JAR, we don't want that
            //val inputGameJar = FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar")
            //if (inputGameJar is Path)
            //return inputGameJar.toFile()

            // This is our best bet towards getting the Intermediary JAR.
            val deobfJar =
                getDeobfJarDir(
                    FabricLoader.getInstance().gameDir,
                    "minecraft",
                    KiltLoader.MC_VERSION.friendlyString
                ) / "${FabricLoader.getInstance().environmentType.name.lowercase()}-${launcher.targetNamespace}.jar"

            if (deobfJar.exists())
                return deobfJar
        } else {
            // TODO: is there a better way of doing this?
            val possibleMcGameJar = launcher.classPath.firstOrNull { path ->
                val str = path.absolutePathString()
                str.contains("net") && str.contains("minecraft") && str.contains("-loom.mappings.") && str.contains("minecraft-merged-")
            }

            return possibleMcGameJar
        }

        return null
    }

    suspend fun getGameClassPath(): Array<out Path> {
        return if (!FabricLoader.getInstance().isDevelopmentEnvironment)
            arrayOf(
                getMCGameFile()
                    ?: FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar") as Path,
                Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath()
            )
        else
            mutableListOf<Path>().apply {
                val remapClasspathFile = System.getProperty(SystemProperties.REMAP_CLASSPATH_FILE)
                    ?: throw RuntimeException("No remapClasspathFile provided")

                val content = withContext(Dispatchers.IO) { Path(remapClasspathFile).readText() }

                this.addAll(
                    content.split(File.pathSeparator.toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .map { Path(it) }
                )

                this.add(Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
            }.toTypedArray()
    }

    private suspend fun remapMinecraft(mappingName: String, mappingFile: IMappingFile): Path {
        val srgFile =
            KiltLoader.kiltCacheDir / "minecraft_${KiltLoader.MC_VERSION.friendlyString}-${mappingName}_$MC_MAPPED_JAR_VERSION.jar"

        if (srgFile.exists() && !forceRemap) {
            logger.info("SRG-mapped Minecraft JAR detected, not creating a new remapped file.")
            return srgFile
        }

        if (gameFile == null) {
            throw IllegalStateException("Minecraft JAR was not found!")
        }

        logger.info("Creating ${mappingName}-mapped Minecraft JAR for remapping Forge mods...")
        val startTime = System.currentTimeMillis()

        val classProvider = ClassProvider.builder().apply {
            this.addLibrary(gameFile)
            for (path in getGameClassPath()) {
                this.addLibrary(path)
            }
        }.build()
        val srgRemapper = EnhancedRemapper(classProvider, mappingFile, logConsumer)

        val gameJar = withContext(Dispatchers.IO) { JarFile(gameFile.toFile()) }
        runCatching { srgFile.createFile() }

        withContext(Dispatchers.IO) { JarOutputStream(srgFile.outputStream()) }.use { outputJar ->
            gameJar.stream().consumeAsFlow().flowOn(Dispatchers.IO)
                .collect { entry ->
                    if (entry.name.endsWith(".class")) {
                        val classReader = gameJar.getInputStream(entry).use { ClassReader(it) }

                        val classNode = ClassNode(Opcodes.ASM9)
                        classReader.accept(classNode, 0)

                        val classWriter = ClassWriter(0)

                        val visitor = EnhancedClassRemapper(classWriter, srgRemapper, RenamingTransformer(srgRemapper, false))
                        classNode.accept(visitor)
                        ConflictingStaticMethodFixer.fixClass(classNode)

                        // We need to remap to the correct name, otherwise the remapper completely fails in production environments.
                        val srgName = mappingFile.remapClass(entry.name.removePrefix("/").removeSuffix(".class"))

                        outputJar.putNextEntry(JarEntry("$srgName.class"))
                        outputJar.write(classWriter.toByteArray())
                        outputJar.closeEntry()
                    } else {
                        outputJar.putNextEntry(entry)
                        gameJar.getInputStream(entry).use { outputJar.write(it.readAllBytes()) }
                        outputJar.closeEntry()
                    }
                }
        }

        logger.info("Remapped Minecraft to $mappingName. (took ${System.currentTimeMillis() - startTime} ms)")

        return srgFile
    }

    fun remapDescriptor(descriptor: String, reverse: Boolean = false, toIntermediary: Boolean = false): String {
        var formedString = ""

        var incompleteString = ""
        var isInClass = false
        descriptor.forEach {
            if (it == 'L' && !isInClass)
                isInClass = true

            if (isInClass) {
                incompleteString += it

                if (it == ';') {
                    isInClass = false

                    formedString += 'L'

                    val name = incompleteString.removePrefix("L").removeSuffix(";")
                    formedString += if (!reverse)
                        remapClass(name, toIntermediary)
                    else
                        unmapClass(name)

                    formedString += ';'

                    incompleteString = ""
                }
            } else {
                formedString += it
            }
        }

        return formedString
    }
}