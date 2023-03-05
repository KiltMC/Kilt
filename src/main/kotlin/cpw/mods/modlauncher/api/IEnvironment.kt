package cpw.mods.modlauncher.api

import java.util.*
import java.util.function.Supplier

interface IEnvironment {
    fun <T> getProperty(key: TypesafeMap.Key<T>?): Optional<T & Any>

    companion object {
        fun <T> buildKey(name: String?, clazz: Class<in T>?): Supplier<TypesafeMap.Key<T>?> {
            return Supplier {
                null
            }
        }
    }

    object Keys {
        /**
         * Version passed in through arguments
         */
        @JvmField
        val VERSION: Supplier<TypesafeMap.Key<String>?> = buildKey("version", String::class.java)
    }
}