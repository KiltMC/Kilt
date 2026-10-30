package xyz.bluspring.kilt.loader.mod.fabric

import cpw.mods.jarhandling.SecureJar
import net.fabricmc.loader.api.ModContainer
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import xyz.bluspring.kilt.loader.KiltModProvider
import java.nio.file.Path
import java.util.function.Supplier
import kotlin.io.path.name

class FabricModFileWrapper(val mod: ModContainer, private val fileInfo: FabricModFileInfoWrapper) : IModFile {
    override fun getLoaders(): List<IModLanguageProvider> {
        return emptyList()
    }

    override fun findResource(vararg pathName: String): Path? {
        return mod.findPath(pathName.joinToString("/")).orElse(null)
    }

    override fun getSubstitutionMap(): Supplier<Map<String?, Any?>> {
        return Supplier { mapOf() }
    }

    override fun getType(): IModFile.Type {
        return IModFile.Type.MOD
    }

    override fun getFilePath(): Path? {
        return mod.rootPaths.first()
    }

    override fun getSecureJar(): SecureJar? {
        return null
    }

    override fun setSecurityStatus(status: SecureJar.Status) {
    }

    override fun getModInfos(): List<IModInfo> {
        return listOf()
    }

    override fun getScanResult(): ModFileScanData {
        return ModFileScanData()
    }

    override fun getFileName(): String {
        return this.mod.rootPaths.first().name
    }

    override fun getProvider(): IModProvider? {
        return KiltModProvider
    }

    override fun getModFileInfo(): IModFileInfo {
        return this.fileInfo
    }
}
