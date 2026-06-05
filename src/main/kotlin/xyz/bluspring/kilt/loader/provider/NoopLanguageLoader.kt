package xyz.bluspring.kilt.loader.provider

import net.neoforged.fml.ModContainer
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.IModLanguageLoader
import net.neoforged.neoforgespi.language.ModFileScanData

object NoopLanguageLoader : IModLanguageLoader {
    override fun name(): String = "kilt_noop"

    override fun version(): String = "0.0.0"

    override fun loadMod(
        info: IModInfo?,
        modFileScanResults: ModFileScanData?,
        layer: ModuleLayer?
    ): ModContainer? {
        throw IllegalStateException()
    }
}
