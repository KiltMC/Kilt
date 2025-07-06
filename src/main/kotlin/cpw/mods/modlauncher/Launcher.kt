package cpw.mods.modlauncher

import cpw.mods.modlauncher.api.IModuleLayerManager
import java.util.*

// Reimplemented this because otherwise Aquaculture does not fucking function
class Launcher private constructor() {
    private val environment = Environment()

    fun environment(): Environment {
        return environment
    }

    fun findLayerManager(): Optional<IModuleLayerManager> {
        return Optional.empty()
    }

    companion object {
        @JvmField
        val INSTANCE: Launcher = Launcher()
    }
}