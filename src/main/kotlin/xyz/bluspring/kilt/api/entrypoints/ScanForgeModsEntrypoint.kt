package xyz.bluspring.kilt.api.entrypoints

import xyz.bluspring.kilt.loader.mod.NeoForgeMod

interface ScanForgeModsEntrypoint {
    fun Collection<NeoForgeMod>.hasMod(id: String): Boolean {
        return this.any { it.modId == id }
    }

    fun Collection<NeoForgeMod>.getMod(id: String): NeoForgeMod? {
        return this.firstOrNull { it.modId == id }
    }

    fun onScanMods(modLoadingQueue: Collection<NeoForgeMod>)
}