package xyz.bluspring.knit.loader.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import xyz.bluspring.knit.loader.KnitLoader
import xyz.bluspring.knit.loader.mod.KnitMod
import xyz.bluspring.knit.loader.mod.ModVersion

class KnitLoaderFabric : KnitLoader<ModContainer>() {
    override fun <T : KnitMod> createContainer(mod: T): ModContainer {
        TODO("Not yet implemented")
    }

    override fun modExistsNatively(id: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(id)
    }

    override fun getNativeModVersion(id: String): ModVersion {
        return FabricModVersion(FabricLoader.getInstance().getModContainer(id).orElseThrow().metadata.version)
    }
}