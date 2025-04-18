package xyz.bluspring.kilt.api.entrypoints

import xyz.bluspring.kilt.loader.mod.ForgeMod

interface ScanForgeModsEntrypoint {
    fun Collection<ForgeMod>.hasMod(id: String): Boolean {
        return this.any { it.modId == id }
    }

    fun Collection<ForgeMod>.getMod(id: String): ForgeMod? {
        return this.firstOrNull { it.modId == id }
    }

    fun onScanMods(modLoadingQueue: Collection<ForgeMod>)
}