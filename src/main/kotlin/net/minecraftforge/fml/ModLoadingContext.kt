package net.minecraftforge.fml

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import xyz.bluspring.kilt.loader.ForgeMod

class ModLoadingContext {
    // this should be Any, but we're only handling Java mods here so
    private var languageExtension: FMLJavaModLoadingContext? = null
    private var mod: ForgeMod? = null

    fun setActiveContainer(container: ModContainer) {

    }

    fun kiltSetActiveMod(mod: ForgeMod) {
        this.mod = mod
        this.languageExtension = FMLJavaModLoadingContext(mod)
    }

    fun extension(): FMLJavaModLoadingContext? {
        return languageExtension
    }

    fun getActiveContainer(): ModContainer? {
        return null
    }

    fun getActiveNamespace(): String {
        return mod?.modInfo?.mod?.modId ?: "minecraft"
    }

    companion object {
        private val context = ThreadLocal.withInitial(::ModLoadingContext)

        @JvmStatic
        fun get(): ModLoadingContext {
            return context.get()
        }
    }
}