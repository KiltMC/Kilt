package xyz.bluspring.kilt.loader.util

import net.fabricmc.loader.api.Version

class VersionRange(private val minVersion: Version?, private val maxVersion: Version?) {
    override fun toString(): String {
        return "${minVersion?.friendlyString ?: ""},${maxVersion?.friendlyString ?: ""}"
    }

    fun isInRange(version: Version): Boolean {
        return if (minVersion != null && maxVersion != null) {
            version >= minVersion && version <= maxVersion
        } else if (minVersion != null) {
            version >= minVersion
        } else if (maxVersion != null) {
            version <= maxVersion
        } else {
            false
        }
    }

    companion object {
        fun parse(rangeString: String): VersionRange {
            val split = rangeString.split(",")
            val minVersion = if (split[0] == "[") null else Version.parse(split[0].removePrefix("["))
            val maxVersion = if (split[1] == ")") null else Version.parse(split[1].removeSuffix(")"))

            return VersionRange(minVersion, maxVersion)
        }
    }
}