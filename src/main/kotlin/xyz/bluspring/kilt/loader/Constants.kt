package xyz.bluspring.kilt.loader

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    // remember to update libs.versions.toml too!
    @JvmField val NEOFORGE_LOADER_VERSION = DefaultArtifactVersion("4.0.45") // 1.21.1
    @JvmField val NEOFORGE_API_VERSION = DefaultArtifactVersion("21.1.266")
}
