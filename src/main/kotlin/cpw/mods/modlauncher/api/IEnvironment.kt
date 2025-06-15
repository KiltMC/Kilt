package cpw.mods.modlauncher.api

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService
import java.nio.file.Path
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

interface IEnvironment {
    fun <T> getProperty(key: TypesafeMap.Key<T>): Optional<T & Any>
    fun <T> computePropertyIfAbsent(key: TypesafeMap.Key<T>, valueFunction: Function<in TypesafeMap.Key<T>, out T>): T
    fun findLaunchPlugin(name: String): Optional<ILaunchPluginService>
    fun findLaunchHandler(name: String): Optional<ILaunchHandlerService>
    fun findModuleLayerManager(): Optional<IModuleLayerManager>

    companion object {
        @JvmStatic
        fun <T> buildKey(name: String, clazz: Class<in T>): Supplier<TypesafeMap.Key<T>> {
            return TypesafeMap.KeyBuilder(name, clazz, IEnvironment::class.java)
        }
    }

    object Keys {
        @JvmField val VERSION = buildKey("version", String::class.java)
        @JvmField val GAMEDIR = buildKey("gamedir", Path::class.java)
        @JvmField val ASSETSDIR = buildKey("assetsdir", Path::class.java)
        @JvmField val UUID = buildKey("uuid", String::class.java)
        @JvmField val LAUNCHTARGET  = buildKey("launchtarget", String::class.java)
        @JvmField val NAMING = buildKey("naming", String::class.java)
    }
}