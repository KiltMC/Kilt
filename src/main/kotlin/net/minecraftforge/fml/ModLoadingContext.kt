package net.minecraftforge.fml

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import xyz.bluspring.kilt.loader.ForgeMod

class ModLoadingContext {
    // this should be Any, but we're only handling Java mods here so
    private var languageExtension: FMLJavaModLoadingContext? = null
    private var mod: ForgeMod? = null

    var activeContainer: ModContainer? = null
    val activeNamespace: String
        get() {
            return mod?.modInfo?.mod?.modId ?: "minecraft"
        }

    fun kiltSetActiveMod(mod: ForgeMod) {
        this.mod = mod
        this.languageExtension = FMLJavaModLoadingContext(mod)
    }

    fun extension(): FMLJavaModLoadingContext? {
        return languageExtension
    }

    companion object {
        private val context = ThreadLocal.withInitial(::ModLoadingContext)

        @JvmStatic
        fun get(): ModLoadingContext {
            return context.get()
        }
    }
}