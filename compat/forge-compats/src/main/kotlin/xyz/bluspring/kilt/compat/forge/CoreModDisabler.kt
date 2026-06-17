package xyz.bluspring.kilt.compat.forge

import xyz.bluspring.kilt.event.LoadCoreModEvent
import xyz.bluspring.kilt.loader.asm.coremod.CoreMod

class CoreModDisabler : LoadCoreModEvent {
    override fun loadCoreMod(coremod: CoreMod): Boolean {
        if (coremod.mod.modId == "attributeslib" && coremod.id == "attributeslib_potion_gui_tooltips") {
            return false
        }
        return true
    }
}
