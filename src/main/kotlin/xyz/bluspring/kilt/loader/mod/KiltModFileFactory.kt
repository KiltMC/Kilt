package xyz.bluspring.kilt.loader.mod

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import net.minecraftforge.forgespi.locating.ModFileFactory

class KiltModFileFactory : ModFileFactory {
    override fun build(
        jar: SecureJar?,
        provider: IModProvider?,
        parser: ModFileFactory.ModFileInfoParser?
    ): IModFile? {
        throw IllegalStateException("Kilt does not currently support directly building mod files via ForgeSPI!")
    }
}