package xyz.bluspring.kilt.api.compatibility

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.Kilt

/**
 * Specifies how the bridge should activate.
 */
sealed class ModBridgeStrategy {
    abstract fun checkValid(fabricModId: String, forgeModId: String)

    /**
     * The variant of mod that is used does not matter, and will bridge if both exist. Default.
     */
    object PreferEither : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, forgeModId: String) {
        }
    }

    /**
     * Throws a failure if only the Forge variant of a mod is available. Will bridge if both sides exist, and will not fail if only the Fabric variant is available.
     */
    open class PreferFabric(private val message: String) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, forgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(forgeModId))
                return

            if (!checkFabricExists(fabricModId) && Kilt.loader.hasMod(forgeModId)) {
                throwException(fabricModId, forgeModId)
            }
        }

        protected open fun throwException(fabricModId: String, forgeModId: String) {
            throw BridgeFailedException(this.message)
        }

        companion object : PreferFabric("") {
            override fun throwException(fabricModId: String, forgeModId: String) {
                throw BridgeFailedException("The Fabric version of mod ID \"${fabricModId}\" also needs to be installed to use Forge mod ID \"$forgeModId\"! THIS MEANS INSTALL BOTH VERSIONS AT THE SAME TIME.")
            }
        }
    }

    /**
     * Throws a failure if only the Fabric variant of a mod is available. Will bridge if both sides exist, and will not fail if only the Forge variant is available.
     */
    open class PreferForge(private val message: String) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, forgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(forgeModId))
                return

            if (checkFabricExists(fabricModId) && !Kilt.loader.hasMod(forgeModId)) {
                throwException(fabricModId, forgeModId)
            }
        }

        protected open fun throwException(fabricModId: String, forgeModId: String) {
            throw BridgeFailedException(this.message)
        }

        companion object : PreferForge("") {
            override fun throwException(fabricModId: String, forgeModId: String) {
                throw BridgeFailedException("The Forge version of mod ID \"${forgeModId}\" also needs to be installed to use mod ${FabricLoader.getInstance().getModContainer(fabricModId).orElseThrow().metadata.name} ($fabricModId)! THIS MEANS INSTALL BOTH VERSIONS AT THE SAME TIME.")
            }
        }
    }

    /**
     * Both Fabric and Forge mods are required in order for the bridge to work. Will fail if either mod is not available, but will not fail if both are unavailable.
     */
    open class RequireBoth @JvmOverloads constructor(val fabricMessage: String, val forgeMessage: String = fabricMessage) : ModBridgeStrategy() {
        override fun checkValid(fabricModId: String, forgeModId: String) {
            if (!FabricLoader.getInstance().isModLoaded(fabricModId) && !Kilt.loader.hasMod(forgeModId))
                return

            if (!checkFabricExists(fabricModId)) {
                throwFabricException(fabricModId, forgeModId)
            }

            if (!Kilt.loader.hasMod(forgeModId)) {
                throwForgeException(fabricModId, forgeModId)
            }
        }

        protected open fun throwFabricException(fabricModId: String, forgeModId: String) {
            throw BridgeFailedException(this.fabricMessage)
        }

        protected open fun throwForgeException(fabricModId: String, forgeModId: String) {
            throw BridgeFailedException(this.forgeMessage)
        }

        companion object : RequireBoth("") {
            override fun throwFabricException(fabricModId: String, forgeModId: String) {
                throw BridgeFailedException("The Fabric version of mod ID \"${fabricModId}\" is also required to use Forge mod ID \"$forgeModId\"! THIS MEANS INSTALL BOTH VERSIONS AT THE SAME TIME.")
            }

            override fun throwForgeException(fabricModId: String, forgeModId: String) {
                throw BridgeFailedException("The Forge version of mod ID \"${forgeModId}\" is also required to use mod ${FabricLoader.getInstance().getModContainer(fabricModId).orElseThrow().metadata.name} ($fabricModId)! THIS MEANS INSTALL BOTH VERSIONS AT THE SAME TIME.")
            }
        }
    }

    companion object {
        @JvmStatic
        fun checkFabricExists(modId: String): Boolean {
            return FabricLoader.getInstance().isModLoaded(modId)
                && FabricLoader.getInstance().getModContainer(modId).orElse(null)?.metadata?.type?.lowercase() != "forge"
        }
    }
}
