package xyz.bluspring.kilt.loader

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.util.FileSystemUtil
import net.neoforged.bus.api.Event
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.Mixins
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.api.KiltWrappedModContainerEntrypoint
import xyz.bluspring.kilt.api.compatibility.BridgeFailedException
import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.workarounds.ModifiedCloneWorkaroundLoader
import xyz.bluspring.knit.loader.KnitLoader
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModEnvironment
import xyz.bluspring.twill.loader.TwillLoader
import xyz.bluspring.twill.loader.TwillOverrides
import xyz.bluspring.twill.loader.fabric.WrappedFabricModContainer
import xyz.bluspring.twill.loader.knit.NeoForgeMod
import kotlin.io.path.createDirectories
import kotlin.io.path.div

class KiltLoader : TwillOverrides {
    private val bridgedModDefinitions = mutableListOf<ModDefinition>()

    // This over here is a wall of shame for mods that use different mod IDs between their Forge and Fabric variants.
    @get:JvmName("getNeoForgeToFabricMods")
    val NEOFORGE_TO_FABRIC_MODS = mapOf(
        // Forge ID -> Fabric ID
        "cloth_config" to "cloth-config",
        "playeranimator" to "player-animator",
    )

    init {
        val loader = FabricLoader.getInstance()

        if (loader.environmentType == EnvType.CLIENT && loader.isModLoaded("sodium")) {
            // If someone wants to fix this, be my guest, drop a PR. Don't send threats of forking Kilt just because you don't like the fact that we don't support Embeddium.
            if (loader.isModLoaded("embeddium") && !KiltFlags.FORCE_ALLOW_BLOCKED_MODS) {
                KnitLoader.instance.displayError(KILT_ERROR_MESSAGE, IllegalStateException("Kilt: You are using Embeddium, which is not supported under Kilt!"))
            }
        }
    }

    override val hasLaunchOverride: Boolean = true

    override fun modExistsNatively(id: String): Boolean {
        // Lie to Knit so we can selectively load stuff from this JAR.
        if (KiltModCompatBridgeManager.canMakeActive(id))
            return false

        return super.modExistsNatively(id)
    }

    override fun getNativeModId(dependencyId: String): String? {
        if (NEOFORGE_TO_FABRIC_MODS.contains(dependencyId))
            return NEOFORGE_TO_FABRIC_MODS[dependencyId]!!

        return super.getNativeModId(dependencyId)
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

        if (this.hasMod("embeddium")) {
            KnitLoader.instance.displayError("Kilt: You are using Embeddium, which is not supported under Kilt!", IllegalStateException())
        } else if (this.hasMod("rubidium")) {
            KnitLoader.instance.displayError("Kilt: You are using Rubidium, which is not supported under Kilt!", IllegalStateException())
        }
    }

    override fun tryMakeActive(mod: NeoForgeMod): Boolean {
        // Don't add the mods that probably shouldn't exist into here.
        val definition = mod.definition
        if (KiltModCompatBridgeManager.canMakeActive(definition.id)) {
            // Directly inject into the classpath.
            KnitLoader.instance.injectIntoClasspath(definition.path)
            this.bridgedModDefinitions.add(definition)
            return true
        }

        return false
    }

    override suspend fun tryRemapMods(definitions: Collection<ModDefinition>) {
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
    }

    override fun preInitialize() {
        ModifiedCloneWorkaroundLoader.load()

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

    override fun loadMods() {
        // Initialize any compatibility bridges that have been registered
        KiltModCompatBridgeManager.processLoadedMods()

        // Let's provide any Fabric mods with their wrapped container entrypoints
        for (container in FabricLoader.getInstance().getEntrypointContainers(KiltWrappedModContainerEntrypoint.ENTRYPOINT, KiltWrappedModContainerEntrypoint::class.java)) {
            container.entrypoint.onLoadModContainer(WrappedFabricModContainer.get(container.provider))
        }
    }

    /*
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
     */

    fun postEvent(ev: Event) {
        mods.forEach {
            it.eventBus.post(ev)
        }
    }

    fun getMod(id: String): NeoForgeMod? {
        return mods.firstOrNull { it != null && it.definition.id == id }
    }

    fun hasMod(id: String): Boolean {
        return TwillLoader.instance.hasMod(id)
    }

    companion object {
        val instance: KiltLoader = KiltLoader()

        const val KILT_ERROR_MESSAGE = "Kilt: Failed to start Kilt, please read the exception below!"

        // These constants are to be updated each time we change versions
        val SUPPORTED_NEO_API_VERSION = Constants.NEOFORGE_API_VERSION
        val MC_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version

        val kiltCacheDir = (FabricLoader.getInstance().gameDir / ".kilt").apply {
            runCatching { this.createDirectories() }
        }
    }
}
