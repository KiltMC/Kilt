package xyz.bluspring.kilt.loader

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    @JvmField val FORGE_LOADER_VERSION = DefaultArtifactVersion("43") // 1.19.2
    @JvmField val FORGE_API_VERSION = DefaultArtifactVersion("43.5.0")

    // Kotlin for Forge version. We're trying to emulate KFF.
    @JvmField val KFF_VERSION = DefaultArtifactVersion("4.10.0")
}