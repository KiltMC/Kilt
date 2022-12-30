package net.minecraftorge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import java.nio.file.Path
import java.util.function.Supplier

class ModFile : IModFile {
    override fun getLoaders(): MutableList<IModLanguageProvider> {
        TODO("Not yet implemented")
    }

    override fun findResource(vararg pathName: String?): Path {
        TODO("Not yet implemented")
    }

    override fun getSubstitutionMap(): Supplier<MutableMap<String, Any>> {
        TODO("Not yet implemented")
    }

    override fun getType(): IModFile.Type {
        TODO("Not yet implemented")
    }

    override fun getFilePath(): Path {
        TODO("Not yet implemented")
    }

    override fun getSecureJar(): cpw.mods.jarhandling.SecureJar {
        TODO("Not yet implemented")
    }

    override fun setSecurityStatus(status: cpw.mods.jarhandling.SecureJar.Status?) {
        TODO("Not yet implemented")
    }

    override fun getModInfos(): MutableList<IModInfo> {
        TODO("Not yet implemented")
    }

    override fun getScanResult(): ModFileScanData {
        TODO("Not yet implemented")
    }

    override fun getFileName(): String {
        TODO("Not yet implemented")
    }

    override fun getProvider(): IModProvider {
        TODO("Not yet implemented")
    }

    override fun getModFileInfo(): IModFileInfo {
        TODO("Not yet implemented")
    }
}