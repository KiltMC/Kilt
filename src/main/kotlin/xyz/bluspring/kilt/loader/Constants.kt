package xyz.bluspring.kilt.loader

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    @JvmField val FORGE_LOADER_VERSION = DefaultArtifactVersion("47") // 1.20.1
    @JvmField val FORGE_API_VERSION = DefaultArtifactVersion("47.4.22")

    // Kotlin for Forge version. We're trying to emulate KFF.
    @JvmField val KFF_VERSION = DefaultArtifactVersion("4.12.0")
    @JvmField val KLF_VERSION = DefaultArtifactVersion("2.12.1-k2.4.10-2.0+forge")

    const val KILT_ERROR_MESSAGE = "Kilt: Failed to start Kilt, please read the exception below!"
}
