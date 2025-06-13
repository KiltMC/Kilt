package xyz.bluspring.kilt.loader

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    @JvmField val NEOFORGE_LOADER_VERSION = DefaultArtifactVersion("4") // 1.21.1
    @JvmField val NEOFORGE_API_VERSION = DefaultArtifactVersion("21.1.179")

    // Kotlin for Forge version. We're trying to emulate KFF.
    @JvmField val KFF_VERSION = DefaultArtifactVersion("5.8.0")
}