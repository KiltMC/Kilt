package xyz.bluspring.kilt.loader

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModDependency
import net.fabricmc.loader.api.metadata.ModOrigin
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.metadata.EntrypointMetadata
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import net.fabricmc.loader.impl.metadata.NestedJarEntry
import java.nio.file.Path
import java.util.*
import kotlin.io.path.toPath

class KiltFabricModContainer(private val mod: ForgeMod) : ModContainerImpl(mod.fabricCandidate) {
    override fun getMetadata(): LoaderModMetadata {
        return mod.loaderMetadata
    }

    override fun getRootPaths(): MutableList<Path> {
        return mod.paths
    }

    override fun getOrigin(): ModOrigin? {
        return null
    }

    override fun getContainingMod(): Optional<net.fabricmc.loader.api.ModContainer> {
        return Optional.empty()
    }

    override fun getContainedMods(): MutableCollection<net.fabricmc.loader.api.ModContainer> {
        return mutableListOf()
    }

    override fun getRootPath(): Path? {
        return mod.paths.firstOrNull()
    }

    override fun getPath(file: String): Path? {
        return mod.modObject::class.java.getResource("/$file")?.toURI()?.toPath()
    }

    override fun getInfo(): LoaderModMetadata {
        return mod.loaderMetadata
    }

    override fun getCodeSourcePaths(): MutableList<Path> {
        return rootPaths
    }

    class FabricModMetadata(private val mod: ForgeMod) : ForgeMod.FabricModMetadata(mod), LoaderModMetadata {
        override fun loadsInEnvironment(type: EnvType?): Boolean {
            return true
        }

        override fun getEntrypoints(type: String?): MutableList<EntrypointMetadata> {
            return mutableListOf()
        }

        override fun getEntrypointKeys(): MutableCollection<String> {
            return mutableListOf()
        }

        override fun getSchemaVersion(): Int {
            return 1
        }

        override fun getLanguageAdapterDefinitions(): MutableMap<String, String> {
            return mutableMapOf()
        }

        override fun getJars(): MutableCollection<NestedJarEntry> {
            return mutableListOf()
        }

        override fun getMixinConfigs(type: EnvType?): MutableCollection<String> {
            return mutableListOf()
        }

        override fun getAccessWidener(): String? {
            return null
        }

        override fun getOldInitializers(): MutableCollection<String> {
            return mutableListOf()
        }

        override fun emitFormatWarnings() {
        }

        override fun setVersion(version: Version?) {
        }

        override fun setDependencies(dependencies: MutableCollection<ModDependency>?) {
        }
    }
}