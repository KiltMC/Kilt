package net.minecraftorge.fml.loading.moddiscovery

import com.mojang.logging.LogUtils
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModFileInfo.LanguageSpec
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.IModFile
import org.slf4j.Logger
import java.net.URL
import java.util.*

/*
class ModFileInfo(
    private val modFile: ModFile,
    private val config: IConfigurable,
    private val languageSpecs: List<LanguageSpec>
) : IModFileInfo, IConfigurable {
    private val LOGGER: Logger = LogUtils.getLogger()
    private val issueURL: URL = URL(config.getConfigElement<String>("issueTrackerURL").orElse(""))
    private val showAsResourcePack: Boolean = config.getConfigElement<Boolean>("showAsResourcePack").orElse(false)
    private val mods: MutableList<IModInfo> = mutableListOf<IModInfo>().apply {
        val modsMetadataList = config.getConfigList("mods")

        modsMetadataList.forEach { metadata ->
            val modId = metadata.getConfigElement<String>("modId").orElseThrow {
                Exception("Forge mod file ${modFile.fileName} does not contain a mod ID!")
            }


        }
    }
    private val properties: Map<String, Any> = mapOf()
    private val license: String = config.getConfigElement<String>("license").get()
    private val usesServices: List<String> = listOf()

    override fun getMods(): MutableList<IModInfo> {
        return mods
    }

    override fun requiredLanguageLoaders(): MutableList<LanguageSpec> {
        TODO("Not yet implemented")
    }

    override fun showAsResourcePack(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getFileProperties(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getLicense(): String {
        TODO("Not yet implemented")
    }

    override fun moduleName(): String {
        TODO("Not yet implemented")
    }

    override fun versionString(): String {
        TODO("Not yet implemented")
    }

    override fun usesServices(): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun getFile(): IModFile {
        TODO("Not yet implemented")
    }

    override fun getConfig(): IConfigurable {
        return config
    }

    override fun <T : Any?> getConfigElement(vararg key: String?): Optional<T> {
        TODO("Not yet implemented")
    }

    override fun getConfigList(vararg key: String?): MutableList<out IConfigurable> {
        TODO("Not yet implemented")
    }

}
*/