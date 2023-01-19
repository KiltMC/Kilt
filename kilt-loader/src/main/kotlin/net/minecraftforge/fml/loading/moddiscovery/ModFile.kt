package net.minecraftforge.fml.loading.moddiscovery

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltModProvider
import java.nio.file.Path
import java.util.function.Supplier

class ModFile(private val kiltMod: ForgeMod) : IModFile {
    override fun getLoaders(): MutableList<IModLanguageProvider> {
        return mutableListOf()
    }

    override fun findResource(vararg pathName: String): Path {
        if (pathName.isEmpty())
            throw IllegalArgumentException("Missing path")

        return kiltMod.getSecureJar().get().getPath(pathName.joinToString("/"))
    }

    override fun getSubstitutionMap(): Supplier<MutableMap<String, Any>> {
        return Supplier {
            mutableMapOf()
        }
    }

    override fun getType(): IModFile.Type {
        return IModFile.Type.MOD
    }

    override fun getFilePath(): Path {
        TODO("Not yet implemented")
    }

    override fun getSecureJar(): SecureJar {
        return kiltMod.getSecureJar().get()
    }

    override fun setSecurityStatus(status: SecureJar.Status?) {

    }

    override fun getModInfos(): MutableList<IModInfo> {
        return mutableListOf(ModInfo(kiltMod))
    }

    override fun getScanResult(): ModFileScanData {
        return ModFileScanData().apply {
            this.addModFileInfo(modFileInfo)
        }
    }

    override fun getFileName(): String {
        return kiltMod.modFile.name
    }

    override fun getProvider(): IModProvider {
        return KiltModProvider()
    }

    override fun getModFileInfo(): IModFileInfo {
        return ModFileInfo(kiltMod)
    }
}