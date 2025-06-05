package xyz.bluspring.kilt.loader.mod

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import xyz.bluspring.knit.loader.mod.VersionConstraint

class ForgeVersionConstraint(val range: VersionRange) : VersionConstraint {
    override fun matches(versionString: String): Boolean {
        return range.containsVersion(DefaultArtifactVersion(versionString))
    }

    override fun toString(): String {
        return range.toString()
    }
}