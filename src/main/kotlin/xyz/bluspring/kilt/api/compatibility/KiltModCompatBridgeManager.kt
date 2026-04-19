package xyz.bluspring.kilt.api.compatibility

import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.knit.loader.api.KnitNativeModCompatExtension

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

    fun register(modId: String, onEnabled: Runnable) {
        this.entries[ModEntry(modId, modId)] = onEnabled
    }

    fun register(fabricModId: String, neoForgeModId: String, onEnabled: Runnable) {
        this.entries[ModEntry(fabricModId, neoForgeModId)] = onEnabled
    }

    fun isActive(fabricModId: String): Boolean {
        val entry = this.entries.keys.firstOrNull { it.fabricModId == fabricModId } ?: return false
        return isActive(entry)
    }

    @ApiStatus.Internal
    internal fun canMakeActive(neoForgeModId: String): Boolean {
        val entry = this.entries.keys.firstOrNull { it.neoForgeModId == neoForgeModId } ?: return false
        return FabricLoader.getInstance().isModLoaded(entry.fabricModId)
    }

    @ApiStatus.Internal
    fun isActive(entry: ModEntry): Boolean {
        return KiltLoader.instance.hasMod(entry.neoForgeModId) && FabricLoader.getInstance().isModLoaded(entry.fabricModId)
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
    data class ModEntry(
        val neoForgeModId: String,
        val fabricModId: String,
    )
}