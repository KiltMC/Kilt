package xyz.bluspring.knit.loader.fabric

import net.fabricmc.loader.api.Version
import xyz.bluspring.knit.loader.mod.ModVersion

class FabricModVersion(val version: Version) : ModVersion {
    override fun toString(): String = version.friendlyString

    override fun compareTo(other: ModVersion): Int {
        return version.compareTo(Version.parse(other.toString()))
    }
}