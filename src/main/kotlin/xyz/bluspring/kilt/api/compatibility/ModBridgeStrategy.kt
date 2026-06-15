package xyz.bluspring.kilt.api.compatibility

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.Kilt

/**
 * Specifies how the bridge should activate.
 */
sealed class ModBridgeStrategy {
    abstract fun checkValid(fabricModId: String, neoForgeModId: String)

    /**
     * The variant of mod that is used does not matter, and will bridge if both exist. Default.
     */
    object PreferEither : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, neoForgeModId: String) {
        }
    }

    /**
     * Throws a failure if only the NeoForge variant of a mod is available. Will bridge if both sides exist, and will not fail if only the Fabric variant is available.
     */
    open class PreferFabric(private val message: String) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, neoForgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(neoForgeModId))
                return

            if (!checkFabricExists(fabricModId) && Kilt.loader.hasMod(neoForgeModId)) {
                throwException(fabricModId, neoForgeModId)
            }
        }

        protected open fun throwException(fabricModId: String, neoForgeModId: String) {
            throw BridgeFailedException(this.message)
        }

        companion object : PreferFabric("") {
            override fun throwException(fabricModId: String, neoForgeModId: String) {
                throw BridgeFailedException("The Fabric version of mod ID \"${fabricModId}\" also needs to be installed to use NeoForge mod ID \"$neoForgeModId\"!")
            }
        }
    }

    /**
     * Throws a failure if only the Fabric variant of a mod is available. Will bridge if both sides exist, and will not fail if only the NeoForge variant is available.
     */
    open class PreferNeoForge(private val message: String) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, neoForgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(neoForgeModId))
                return

            if (checkFabricExists(fabricModId) && !Kilt.loader.hasMod(neoForgeModId)) {
                throwException(fabricModId, neoForgeModId)
            }
        }

        protected open fun throwException(fabricModId: String, neoForgeModId: String) {
            throw BridgeFailedException(this.message)
        }

        companion object : PreferNeoForge("") {
            override fun throwException(fabricModId: String, neoForgeModId: String) {
                throw BridgeFailedException("The NeoForge version of mod ID \"${neoForgeModId}\" also needs to be installed to use mod ${FabricLoader.getInstance().getModContainer(fabricModId).orElseThrow().metadata.name} ($fabricModId)!")
            }
        }
    }

    /**
     * Both Fabric and NeoForge mods are required in order for the bridge to work. Will fail if either mod is not available, but will not fail if both are unavailable.
     */
    open class RequireBoth @JvmOverloads constructor(val fabricMessage: String, val neoForgeMessage: String = fabricMessage) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, neoForgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(neoForgeModId))
                return

            if (!checkFabricExists(fabricModId)) {
                throwFabricException(fabricModId, neoForgeModId)
            }

            if (!Kilt.loader.hasMod(neoForgeModId)) {
                throwNeoForgeException(fabricModId, neoForgeModId)
            }
        }

        protected open fun throwFabricException(fabricModId: String, neoForgeModId: String) {
            throw BridgeFailedException(this.fabricMessage)
        }

        protected open fun throwNeoForgeException(fabricModId: String, neoForgeModId: String) {
            throw BridgeFailedException(this.neoForgeMessage)
        }

        companion object : RequireBoth("") {
            override fun throwFabricException(fabricModId: String, neoForgeModId: String) {
                throw BridgeFailedException("The Fabric version of mod ID \"${fabricModId}\" is also required to use NeoForge mod ID \"$neoForgeModId\"!")
            }

            override fun throwNeoForgeException(fabricModId: String, neoForgeModId: String) {
                throw BridgeFailedException("The NeoForge version of mod ID \"${neoForgeModId}\" is also required to use mod ${FabricLoader.getInstance().getModContainer(fabricModId).orElseThrow().metadata.name} ($fabricModId)!")
            }
        }
    }

    companion object {
        @JvmStatic
        fun checkFabricExists(modId: String): Boolean {
            return FabricLoader.getInstance().isModLoaded(modId)
                && FabricLoader.getInstance().getModContainer(modId).orElse(null)?.metadata?.type?.lowercase() != "neoforge"
        }
    }
}
