package cpw.mods.modlauncher.api

import java.util.*

interface IModuleLayerManager {
    fun getLayer(layer: Layer): Optional<ModuleLayer>

    enum class Layer(vararg parent: Layer) {
        BOOT, SERVICE(BOOT), PLUGIN(BOOT), GAME(PLUGIN, SERVICE)
    }
}