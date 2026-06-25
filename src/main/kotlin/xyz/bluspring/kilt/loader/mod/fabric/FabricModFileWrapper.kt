package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.api.ModContainer
import net.neoforged.fml.jarcontents.JarContents
import net.neoforged.neoforgespi.language.IModFileInfo
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.ModFileScanData
import net.neoforged.neoforgespi.locating.IModFile
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes
import java.nio.file.Path
import java.util.function.Supplier
import kotlin.io.path.name

class FabricModFileWrapper(val mod: ModContainer, private val fileInfo: FabricModFileInfoWrapper) : IModFile {
    override fun getId(): String {
        return this.mod.metadata.id
    }

    override fun getContents(): JarContents? {
        return JarContents.ofPaths(this.mod.rootPaths)
    }

    override fun getSubstitutionMap(): Supplier<Map<String, Any>> {
        return { mapOf() }
    }

    override fun getType(): IModFile.Type {
        return IModFile.Type.MOD
    }

    override fun getFilePath(): Path? {
        return mod.rootPaths.first()
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

    override fun getDiscoveryAttributes(): ModFileDiscoveryAttributes? {
        return ModFileDiscoveryAttributes.DEFAULT
    }

    override fun getModFileInfo(): IModFileInfo {
        return this.fileInfo
    }
}
