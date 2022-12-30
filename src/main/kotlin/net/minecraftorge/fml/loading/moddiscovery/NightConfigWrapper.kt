package net.minecraftorge.fml.loading.moddiscovery

import com.electronwill.nightconfig.core.UnmodifiableConfig
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import java.util.*

class NightConfigWrapper(private val config: UnmodifiableConfig) : IConfigurable {
    /*private lateinit var file: IModFileInfo

    fun setFile(file: IModFileInfo): NightConfigWrapper {
        this.file = file
        return this
    }*/

    override fun <T : Any?> getConfigElement(vararg key: String): Optional<T> {
        return config.getOptional<T>(key.joinToString(".")).map {
            if (it is UnmodifiableConfig)
                it.valueMap() as T
            else
                it as T
        }
    }

    override fun getConfigList(vararg key: String): MutableList<out IConfigurable> {
        val path = key.joinToString(".")

        if (config.contains(path) && config.get<Collection<*>>(path) !is Collection<*>) {
            throw Exception("The configuration path $path is invalid. Expecting a collection!")
        }

        val nestedConfigs = config.getOrElse<Collection<UnmodifiableConfig>>(path, ::ArrayList)
        return nestedConfigs.stream()
            .map(::NightConfigWrapper)
            //.map { it.setFile(file) }
            .toList()
    }
}