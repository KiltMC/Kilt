package xyz.bluspring.kilt.api.compatibility

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension
import xyz.bluspring.knit.loader.mod.ModEnvironment

/**
 * Registers mod compatibility bridges into Kilt, for allowing events from the same mod on both Fabric and NeoForge to work together. Kilt will continue to inject the mods into Fabric Loader, but it will not
 * initialize them. Take extra caution to not accidentally start a chain reaction of initializing classes from the Neo mod.
 *
 * These should be registered during [KnitNativeModCompatExtension.setupModScanning], so Kilt can be sure to load the required classes.
 */
object KiltModCompatBridgeManager {
    private val logger = LoggerFactory.getLogger(KiltModCompatBridgeManager::class.java)

    @ApiStatus.Internal
    internal val entries = mutableMapOf<ModEntry, Runnable>()

    @JvmOverloads
    fun register(fabricModId: String, enabledMixinConfigs: Collection<String>, strategy: ModBridgeStrategy = ModBridgeStrategy.PreferEither, environment: ModEnvironment = ModEnvironment.BOTH, onEnabled: Runnable) {
        register(fabricModId, fabricModId, enabledMixinConfigs, strategy, environment, onEnabled)
    }

    @JvmOverloads
    fun register(fabricModId: String, neoForgeModId: String = fabricModId, enabledMixinConfigs: Collection<String> = emptyList(), strategy: ModBridgeStrategy = ModBridgeStrategy.PreferEither, environment: ModEnvironment = ModEnvironment.BOTH, onEnabled: Runnable) {
        this.entries[ModEntry(fabricModId, neoForgeModId, enabledMixinConfigs, strategy, environment)] = onEnabled
    }

    fun isActive(fabricModId: String): Boolean {
        val entry = getModEntryFabric(fabricModId) ?: return false
        return isActive(entry)
    }

    @ApiStatus.Internal
    internal fun canMakeActive(neoForgeModId: String): Boolean {
        val entry = getModEntryNeo(neoForgeModId) ?: return false
        return ModBridgeStrategy.checkFabricExists(entry.fabricModId)
    }

    @ApiStatus.Internal
    fun isActive(entry: ModEntry): Boolean {
        if (entry.environment != ModEnvironment.BOTH) {
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && entry.environment != ModEnvironment.CLIENT)
                return false

            if (FabricLoader.getInstance().environmentType == EnvType.SERVER && entry.environment != ModEnvironment.SERVER)
                return false
        }

        return KiltLoader.instance.hasMod(entry.neoForgeModId) && ModBridgeStrategy.checkFabricExists(entry.fabricModId)
    }

    @ApiStatus.Internal
    internal fun getModEntryFabric(fabricModId: String): ModEntry? {
        return this.entries.keys.firstOrNull { it.fabricModId == fabricModId }
    }

    @ApiStatus.Internal
    internal fun getModEntryNeo(neoForgeModId: String): ModEntry? {
        return this.entries.keys.firstOrNull { it.neoForgeModId == neoForgeModId }
    }

    @ApiStatus.Internal
    internal fun processLoadedMods() {
        for ((entry, onEnabled) in this.entries) {
            if (isActive(entry)) {
                try {
                    onEnabled.run()
                    logger.info("Registered Fabric <-> NeoForge compatibility bridge for mod ${entry.fabricModId}")
                } catch (e: Throwable) {
                    logger.error("Failed to register Fabric <-> NeoForge compatibility bridge for mods ${entry.fabricModId} <-> ${entry.neoForgeModId}!", e)
                }
            }
        }
    }

    @ApiStatus.Internal
    @JvmRecord
    data class ModEntry(
        val neoForgeModId: String,
        val fabricModId: String,
        val enabledMixinConfigs: Collection<String>,
        val strategy: ModBridgeStrategy,
        val environment: ModEnvironment,
    )
}
