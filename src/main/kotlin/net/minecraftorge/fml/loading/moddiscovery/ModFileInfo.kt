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


class ModFileInfo(
    private val modFile: ModFile,
    private val config: IConfigurable,
    private val languageSpecs: List<LanguageSpec>
) : IModFileInfo, IConfigurable {
    private val LOGGER: Logger = LogUtils.getLogger()
    private val config: IConfigurable
    private val modFile: ModFile
    private val issueURL: URL
    private val languageSpecs: List<LanguageSpec>
    private val showAsResourcePack
    private val mods: List<IModInfo>
    private val properties: Map<String, Any>
    private val license: String
    private val usesServices: List<String>

    init {

    }

    override fun getMods(): MutableList<IModInfo> {
        TODO("Not yet implemented")
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
