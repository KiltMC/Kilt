package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.api.metadata.ModOrigin
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.nio.file.Path
import java.util.*
import kotlin.io.path.toPath

class KiltFabricModContainer(private val mod: ForgeMod) : ModContainerImpl(FabricModProvider.instance.createModCandidate(mod)) {
    private val loaderMetadata = FabricModProvider.instance.createLoaderMetadata(mod)

    override fun getMetadata(): LoaderModMetadata {
        return loaderMetadata
    }

    override fun getOrigin(): ModOrigin? {
        return null
    }

    override fun getContainingMod(): Optional<net.fabricmc.loader.api.ModContainer> {
        return Optional.ofNullable(mod.parent?.container?.fabricModContainer)
    }

    override fun getContainedMods(): MutableCollection<net.fabricmc.loader.api.ModContainer> {
        return mod.nestedMods.map { it.container.fabricModContainer }.toMutableList()
    }

    override fun getPath(file: String): Path? {
        return mod.modObject::class.java.getResource("/$file")?.toURI()?.toPath()
    }

    override fun getInfo(): LoaderModMetadata {
        return loaderMetadata
    }

    override fun getCodeSourcePaths(): MutableList<Path> {
        return rootPaths
    }
}