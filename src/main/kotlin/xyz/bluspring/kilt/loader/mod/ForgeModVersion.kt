package xyz.bluspring.kilt.loader.mod

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import xyz.bluspring.knit.loader.mod.ModVersion

class ForgeModVersion(val forgeVersion: ArtifactVersion) : ModVersion {
    override fun toString(): String = forgeVersion.toString()

    override fun compareTo(other: ModVersion): Int {
        return forgeVersion.compareTo(DefaultArtifactVersion(other.toString()))
    }
}