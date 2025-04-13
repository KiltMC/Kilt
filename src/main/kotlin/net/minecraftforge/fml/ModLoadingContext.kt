package net.minecraftforge.fml

import net.minecraftforge.fml.config.IConfigSpec
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltModContainer
import xyz.bluspring.kilt.loader.VanillaModContainer
import java.util.function.BiPredicate
import java.util.function.Supplier

open class ModLoadingContext {
    // this should be Any, but we're only handling Java mods here so
    private val languageExtension: FMLJavaModLoadingContext
        get() {
            return this as? FMLJavaModLoadingContext ?: FMLJavaModLoadingContext.kiltGetContext((this.kiltActiveContainer as KiltModContainer).mod)
        }

    internal var kiltActiveContainer: ModContainer? = null

    fun getActiveContainer(): ModContainer {
        return if (kiltActiveContainer == null)
            VanillaModContainer
        else
            kiltActiveContainer!!
    }

    fun setActiveContainer(container: ModContainer?) {
        this.kiltActiveContainer = container
    }

    val activeNamespace: String
        get() {
            return this.getActiveContainer().namespace
        }

    fun extension(): FMLJavaModLoadingContext {
        return languageExtension
    }

    fun <T> registerExtensionPoint(point: Class<out IExtensionPoint<T>>, extension: Supplier<T>) where T : Record, T : IExtensionPoint<T> {
        this.getActiveContainer().registerExtensionPoint(point, extension)
    }

    // Thank gOD ForgeConfigApiPort uses a different package name for ModLoadingContext, otherwise
    // this wouldn't work well at all.
    fun registerConfig(type: ModConfig.Type, spec: IConfigSpec<*>, fileName: String) {
        val modId = this.activeNamespace
        net.minecraftforge.api.ModLoadingContext.registerConfig(modId, type, spec, fileName)
    }

    fun registerConfig(type: ModConfig.Type, spec: IConfigSpec<*>) {
        val modId = this.activeNamespace
        net.minecraftforge.api.ModLoadingContext.registerConfig(modId, type, spec)
    }

    // TODO: properly implement display tests?
    fun registerDisplayTest(displayTest: IExtensionPoint.DisplayTest) {
    }

    fun registerDisplayTest(displayTest: Supplier<IExtensionPoint.DisplayTest>) {
    }

    fun registerDisplayTest(version: String, remoteVersion: BiPredicate<String, Boolean>) {
    }

    fun registerDisplayTest(suppliedVersion: Supplier<String>, remoteVersion: BiPredicate<String, Boolean>) {
    }

    companion object {
        // ah.
        private val context = ThreadLocal.withInitial(::ModLoadingContext)

        var kiltActiveModId: String?
            get() {
                return this.get().kiltActiveContainer?.modId
            }
            set(value) {
                if (value == null)
                    this.get().setActiveContainer(null)
                else
                    this.get().setActiveContainer(Kilt.loader.getMod(value)?.container ?: throw IllegalStateException("Failed to get container for mod ID $value!"))
            }

        @JvmStatic
        fun get(): ModLoadingContext {
            return this.context.get()
        }
    }
}