package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.FabricLoader
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object Constants {
    @JvmField val NEOFORGE_API_VERSION = DefaultArtifactVersion(FabricLoader.getInstance().getModContainer("kilt").orElseThrow().metadata.getCustomValue("kilt:neoforge_version").asString)
}
