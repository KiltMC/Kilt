package xyz.bluspring.knit.loader.quilt

import org.quiltmc.loader.api.Version
import xyz.bluspring.knit.loader.mod.ModVersion

class QuiltModVersion(val version: Version) : ModVersion {
    override val asString: String
        get() = version.toString()

    override fun compareTo(other: ModVersion): Int {
        return version.compareTo(Version.of(other.asString))
    }
}