package xyz.bluspring.knit.loader.quilt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.quiltmc.loader.api.LoaderValue
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.plugin.QuiltLoaderPlugin
import org.quiltmc.loader.api.plugin.QuiltPluginContext
import xyz.bluspring.knit.loader.KnitLoader
import xyz.bluspring.knit.loader.KnitModLoader
import xyz.bluspring.knit.loader.mod.KnitMod
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModVersion

class KnitLoaderQuilt : KnitLoader<ModContainer>(), QuiltLoaderPlugin {
    private lateinit var context: QuiltPluginContext

    override fun <T : KnitMod> createContainer(mod: T): ModContainer {
        TODO("Not yet implemented")
    }

    override fun modExistsNatively(id: String): Boolean {
        return QuiltLoader.isModLoaded(id)
    }

    override fun getNativeModVersion(id: String): ModVersion {
        return QuiltModVersion(QuiltLoader.getModContainer(id).orElseThrow().metadata().version())
    }

    override fun load(
        context: QuiltPluginContext,
        previousData: Map<String, LoaderValue>
    ) {
        this.context = context

        runBlocking(Dispatchers.IO) {
            scanMods(context.manager().gameDirectory)
        }
    }

    override fun validateDependencies(definitions: Map<ModDefinition, KnitModLoader<*>>) {

    }

    override fun unload(data: Map<String?, LoaderValue?>?) {
    }
}