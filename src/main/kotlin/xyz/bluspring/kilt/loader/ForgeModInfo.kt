package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.Version
import xyz.bluspring.kilt.loader.util.VersionRange

data class ForgeModInfo(
    val license: String = "All Rights Reserved",
    val issueTrackerURL: String = "",
    val showAsResourcePack: Boolean = false,
    val mod: ModMetadata
) {
    data class ModMetadata(
        val modId: String,
        val version: Version,
        val displayName: String,
        val updateJSONURL: String = "",
        val logoFile: String = "",
        val credits: String = "",
        val authors: String = "",
        val description: String,
        val displayTest: DisplayTest = DisplayTest.MATCH_VERSION,
        val dependencies: List<ModDependency>
    ) {
        enum class DisplayTest {
            MATCH_VERSION,
            IGNORE_SERVER_VERSION,
            IGNORE_ALL_VERSION,
            NONE
        }
    }

    data class ModDependency(
        val modId: String,
        val mandatory: Boolean,
        val versionRange: VersionRange,
        val ordering: ModOrdering = ModOrdering.NONE,
        val side: ModSide = ModSide.BOTH
    ) {
        enum class ModOrdering {
            NONE, BEFORE, AFTER
        }

        enum class ModSide {
            BOTH, CLIENT, SERVER
        }
    }
}