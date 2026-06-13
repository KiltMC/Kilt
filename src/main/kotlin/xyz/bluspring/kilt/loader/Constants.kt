package xyz.bluspring.kilt.loader

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    @JvmField val NEOFORGE_LOADER_VERSION = DefaultArtifactVersion("11") // 26.1.2
    @JvmField val NEOFORGE_API_VERSION = DefaultArtifactVersion("26.1.2.76")

    // Kotlin for Forge version. We're trying to emulate KFF.
    @JvmField val KFF_VERSION = DefaultArtifactVersion("5.8.0")
}
