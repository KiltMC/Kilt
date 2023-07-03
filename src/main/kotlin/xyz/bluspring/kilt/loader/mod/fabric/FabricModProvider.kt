package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.discovery.ModCandidate
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.mod.LoaderModProvider

// This has been split to be over here for organization, and also the possibility for Quilt support.
// However, as far as I can tell, loader plugins might be needed in order for Quilt to be supported.
// So for right now, Quilt is not supported.
class FabricModProvider : LoaderModProvider {
    init {
        instance = this
    }

    fun createLoaderMetadata(mod: ForgeMod): LoaderModMetadata {
        return FabricModMetadata(mod)
    }

    fun createModCandidate(mod: ForgeMod): ModCandidate {
        //createPlain(List<Path> paths, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidate> nestedMods)
        val createPlainMethod = ModCandidate::class.java.getDeclaredMethod("createPlain", List::class.java, LoaderModMetadata::class.java, Boolean::class.java, Collection::class.java)
        createPlainMethod.isAccessible = true

        return createPlainMethod.invoke(this, mod.paths, createLoaderMetadata(mod), false, mutableListOf<ModCandidate>().apply {
            mod.nestedMods.forEach {
                this.add(createModCandidate(it))
            }
        }) as ModCandidate
    }

    override fun addModToLoader(mod: ForgeMod) {
        FabricLoaderImpl.INSTANCE.modsInternal.add(mod.container.fabricModContainer)

        val modMapField = FabricLoaderImpl::class.java.getDeclaredField("modMap")
        modMapField.isAccessible = true
        val modMap = modMapField.get(FabricLoaderImpl.INSTANCE) as MutableMap<String, ModContainerImpl>

        modMap[mod.modId] = mod.container.fabricModContainer
    }

    companion object {
        lateinit var instance: FabricModProvider
    }
}