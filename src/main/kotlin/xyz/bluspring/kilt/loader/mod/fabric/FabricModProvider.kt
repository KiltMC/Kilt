package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.discovery.ModCandidateImpl
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.mod.LoaderModProvider

// This has been split to be over here for organization, and also the possibility for Quilt support.
// However, as far as I can tell, loader plugins might be needed in order for Quilt to be supported.
// So for right now, Quilt is not supported.
class FabricModProvider : LoaderModProvider {
    override val name: String = "Fabric Loader"

    private val candidates = mutableMapOf<ForgeMod, ModCandidateImpl>()
    private val loaderMetadatas = mutableMapOf<ForgeMod, LoaderModMetadata>()

    init {
        instance = this
    }

    fun getModCandidate(mod: ForgeMod): ModCandidateImpl {
        return candidates[mod]!!
    }

    fun getLoaderMetadata(mod: ForgeMod): LoaderModMetadata {
        return loaderMetadatas[mod]!!
    }

    fun createLoaderMetadata(mod: ForgeMod): LoaderModMetadata {
        if (loaderMetadatas.containsKey(mod))
            return getLoaderMetadata(mod)

        return FabricModMetadata(mod).apply {
            loaderMetadatas[mod] = this
        }
    }

    fun createModCandidate(mod: ForgeMod): ModCandidateImpl {
        if (candidates.containsKey(mod))
            return getModCandidate(mod)

        //createPlain(List<Path> paths, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidate> nestedMods)
        val createPlainMethod = ModCandidateImpl::class.java.getDeclaredMethod("createPlain", List::class.java, LoaderModMetadata::class.java, Boolean::class.java, Collection::class.java)
        createPlainMethod.isAccessible = true

        val metadata = createLoaderMetadata(mod)

        return createPlainMethod.invoke(this, mod.paths, metadata, false, mutableListOf<ModCandidateImpl>().apply {
            mod.nestedMods.forEach {
                this.add(createModCandidate(it))
            }
        }) as ModCandidateImpl
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