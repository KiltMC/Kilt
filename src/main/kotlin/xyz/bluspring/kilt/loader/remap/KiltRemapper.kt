package xyz.bluspring.kilt.loader.remap

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.stream.consumeAsFlow
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.remap.fixers.*
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinAdditionalRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinDirectModifierFixer
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinStaticMethodFixer
import xyz.bluspring.kilt.loader.remap.resource.IgnoreSignatureResourceRemapper
import xyz.bluspring.kilt.loader.remap.resource.ManifestResourceRemapper
import xyz.bluspring.kilt.util.CaseInsensitiveStringHashSet
import xyz.bluspring.kilt.util.ClassNameHashSet
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.workarounds.ModifiedCloneWorkaroundLoader
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.util.collect
import xyz.bluspring.knit.loader.util.concurrent
import xyz.bluspring.knit.loader.util.launchIn
import xyz.bluspring.knit.loader.util.onEach
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
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
    const val REMAPPER_VERSION = 246
    const val MC_MAPPED_JAR_VERSION = 9

    // Kilt JVM flags
    private val forceRemap = KiltFlags.FORCE_REMAPPING
    private val disableRemaps = KiltFlags.DISABLE_REMAPPING

    internal val logger = LoggerFactory.getLogger("Kilt Remapper")
    // internal val useNamed = FabricLoader.getInstance().mappingResolver.currentRuntimeNamespace != "intermediary"

    private lateinit var remappedModsDir: Path

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
            logger.warn("Forced remaps enabled! All NeoForge mods will be remapped.")

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

        val exception = RuntimeException("Failed to remap NeoForge mods in Kilt!")

        logger.info("Remapping NeoForge mods...")

        val mods = modLoadingQueue.filter { !it.isBuiltin }.toSet()

        val resourceRemappers = listOf(
            ManifestResourceRemapper,
            IgnoreSignatureResourceRemapper
        )

        suspend fun remapMod(file: Path, mod: ModDefinition) {
            val exception = RuntimeException("Failed to remap NeoForge mod ${mod.displayName} (${mod.id})!")
            var allowSoftFail = false

            if (mod.isBuiltin) { // Prevent Kilt from remapping *directly* NeoForge mods. Yes, that started happening.
                return
            }

            // If JiJ'd libraries fail to remap, we can mark it as a "soft failure".
            if (mod.additionalData["isJiJ"] == true && (mod.additionalData["manifest"] as? Manifest?)?.mainAttributes?.getValue("FMLModType") != "GAMELIBRARY") {
                allowSoftFail = true
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
            // val classesToProcess = entryToClassNodes.values.intersect(KiltHelper.getForgeClassNodes().toSet()).toList()

            val mixinClasses = Collections.synchronizedSet(ClassNameHashSet())
            val refmaps = Collections.synchronizedSet(CaseInsensitiveStringHashSet())
            val mixinRefmaps = Collections.synchronizedMap<JarEntry, MixinRefmap>(mutableMapOf())

            // JAR validation information stripping.
            // If we can find out how to use this to our advantage prior to remapping,
            // we may still be able to use this information safely.
            val manifestEntry = jar.getJarEntry("META-INF/MANIFEST.MF")
            if (manifestEntry != null) {
                val manifest = jar.getInputStream(manifestEntry).use { Manifest(it) }
                val mixinConfigs = manifest.mainAttributes.getValue("MixinConfigs")?.split(",")?.toMutableSet() ?: mutableSetOf()

                // Search for more mixin configs, because apparently it's possible to define with a fucking class.
                for (entry in jar.stream()) {
                    if (entry.name.endsWith(".mixin.json") || entry.name.endsWith(".mixins.json")) {
                        mixinConfigs.add(entry.name)
                    } else if (entry.name.endsWith(".refmap.json")) {
                        // Search for more refmaps too, because they're probably defined in class too.
                        refmaps.add(entry.name)
                    }
                }

                // Read mixin configs and add them to the list of mixins to fix
                mixinConfigs.asFlow().collect { config ->
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
                        if (!it.isJsonNull)
                            mixinClasses.add("$mixinPackage.${it.asString}")
                    }

                    runCatching { json.get("refmap")!!.asString }.onSuccess { refmaps.add(it) }
                }
            }

            withContext(Dispatchers.IO) {
                val seen = HashSet<String>()
                jar.stream().consumeAsFlow().collect { entry ->
                    // Ars Nouveau's jar has duplicate entries. wtf?????
                    if (!seen.add(entry.name)) {
                        logger.warn("Mod ${mod.displayName} (${mod.id})'s jar file is malformed; saw duplicate of ${entry.name}!")
                        return@collect
                    }
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
                            val obj = json.asJsonObject

                            // Be careful, because they might not even have the data in the first place.
                            if (obj.has("mappings")) {
                                mixinRefmaps[entry] = MixinRefmap(
                                    Collections.synchronizedMap(
                                        obj.getAsJsonObject("mappings").asMap()
                                            .map {
                                                it.key to it.value.asJsonObject.asMap().map { b ->
                                                    b.key to b.value.asString
                                                }
                                                    .associate { b -> b.first to b.second }
                                                    .toMutableMap()
                                            }
                                            .associate { it.first to it.second }
                                            .toMutableMap()
                                    ),
                                    Collections.synchronizedMap(mutableMapOf())
                                )
                            }
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

            fun isMixinClass(node: ClassNode): Boolean {
                return node.name in mixinClasses ||
                    // GUESS WHAT, SOME MODS DON'T FUCKING DEFINE SOME MIXINS IN THE FILE, INSTEAD IN THE MIXIN PLUGIN.
                    // SO LET'S JUST RUN THIS ON EVERYTHING THAT HAS THE BLOODY ANNOTATION.
                    KiltHelper.mergeNullableCollections(node.visibleAnnotations, node.invisibleAnnotations)
                        .any { it.desc == MixinAdditionalRemapper.MIXIN_TYPE.descriptor }
            }

            // Make copies of the original ClassNode objects so we can use them as reference to remap inherited shadows.
            val originalMappings = mutableMapOf<String, ClassNode>()
            for (node in entryToClassNodes.values) {
                if (isMixinClass(node)) {
                    val writer = ClassWriter(0)
                    node.accept(writer)
                    val nodeCopy = ClassReader(writer.toByteArray())
                    val newNode = ClassNode()
                    nodeCopy.accept(newNode, 0)
                    originalMappings[node.name] = newNode
                }
            }

            val throwable = entryToClassNodes.entries.asFlow().concurrent().runCatching {
                this.collect { (entry, originalNode) ->
                    try {
                        val remappedNode = ClassNode(Opcodes.ASM9)

                        // only do this on mixin classes, please
                        // We must remap the mixins before actually remapping them to Intermediary, so the names are correct in prod.
                        if (isMixinClass(originalNode)) {
                            if (!KiltFlags.DISABLE_FIXERS) {
                                MixinAdditionalRemapper.remapClass(originalNode)
                                MixinStaticMethodFixer.fixClass(originalNode)
                                MixinDirectModifierFixer.fixClass(originalNode)
                            }
                        }

                        if (!KiltFlags.DISABLE_FIXERS) {
                            ConditionalInterfaceInjectionFixer.fixClass(remappedNode)
                            EventClassVisibilityFixer.fixClass(remappedNode)
                            InjectedInterfaceVisibilityFixer.fixClass(remappedNode)
                            ObjectHolderDefinalizer.processClass(remappedNode)
                            WorkaroundFixer.fixClass(remappedNode)
                            ConflictingStaticMethodFixer.fixClass(remappedNode)
                            EnvironmentRemapper.remapClass(remappedNode)
                            EnvironmentLambdaFixer.fixClass(remappedNode)
                            RemoveModulesFixer.fixClass(remappedNode)
                            ModifiedCloneWorkaroundLoader.fixClass(remappedNode)
                            GLVersionSpecifierFixer.fixClass(remappedNode)
                        }

                        val writer = ClassWriter(Opcodes.ASM9)
                        remappedNode.accept(writer)

                        synchronized(jarOutputStream) {
                            jarOutputStream.putNextEntry(entry)
                            jarOutputStream.write(writer.toByteArray())
                            jarOutputStream.closeEntry()
                        }
                    } catch (e: Throwable) {
                        throw RuntimeException("Failed to remap ${entry.name}", e)
                    }
                }
            }.exceptionOrNull()

            if (throwable != null) {
                exception.addSuppressed(throwable)
            }

            jarOutputStream.close()
            jar.close()

            if (exception.suppressed.isNotEmpty()) {
                // Soft fail if a JiJ'd mod failed to remap.
                if (allowSoftFail) {
                    logger.warn("Failed to remap ${mod.id}, but it's a library mod so we should be able to safely ignore it.", exception)
                    return
                }

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
            logger.error("Ran into some errors during the remapping process, cannot continue!")
            throw exception
        }
    }
}
