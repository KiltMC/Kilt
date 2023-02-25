package cpw.mods.modlauncher

import cpw.mods.modlauncher.api.IEnvironment
import cpw.mods.modlauncher.api.TypesafeMap
import java.util.*

class Environment : IEnvironment {
    override fun <T> getProperty(key: TypesafeMap.Key<T>?): Optional<T & Any> {
        return Optional.empty()
    }
}