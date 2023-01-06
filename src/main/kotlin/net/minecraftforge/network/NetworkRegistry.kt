package net.minecraftforge.network

import net.minecraft.resources.ResourceLocation
import java.util.function.Predicate
import java.util.function.Supplier

class NetworkRegistry {
    companion object {
        private val instances = mutableMapOf<ResourceLocation, NetworkInstance>()
        @JvmStatic
        val ABSENT = "ABSENT \uD83E\uDD14"
        @JvmStatic
        val ACCEPTVANILLA = "ALLOWVANILLA \uD83D\uDC93\uD83D\uDC93\uD83D\uDC93"

        @JvmStatic
        fun acceptMissingOr(protocolVersion: String): Predicate<String> {
            return acceptMissingOr(protocolVersion::equals)
        }

        @JvmStatic
        fun acceptMissingOr(versionCheck: Predicate<String>): Predicate<String> {
            return versionCheck.or(ABSENT::equals).or(ACCEPTVANILLA::equals)
        }

        @JvmStatic
        val serverNonVanillaNetworkMods: List<String>
            get() {
                return listOf()
            }

        @JvmStatic
        val clientNonVanillaNetworkMods: List<String>
            get() {
                return listOf()
            }

        @JvmStatic
        fun acceptsVanillaClientConnections(): Boolean {
            return (instances.isEmpty() || serverNonVanillaNetworkMods.isEmpty()) && DatapackRegistriesHooks.syncedCustomRegistries.isEmpty()
        }

        @JvmStatic
        fun canConnectToVanillaServer(): Boolean {
            return instances.isEmpty() || clientNonVanillaNetworkMods.isEmpty()
        }

        fun newSimpleChannel(name: ResourceLocation, networkProtocolVersion: Supplier<String>, clientAcceptedVersions: Predicate<String>, serverAcceptedVersions: Predicate<String>): SimpleChannel {

        }
    }
}