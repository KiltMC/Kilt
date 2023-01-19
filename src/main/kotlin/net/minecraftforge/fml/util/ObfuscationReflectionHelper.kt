package net.minecraftforge.fml.util

import cpw.mods.modlauncher.api.INameMappingService
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object ObfuscationReflectionHelper {
    private val srgIntermediaryTree = KiltRemapper.srgIntermediaryTree
    private val srgToIntermediaryCache = mutableMapOf<String, String?>()
    private val srgToNamedCache = mutableMapOf<String, String>()
    private val isDev = FabricLoader.getInstance().isDevelopmentEnvironment

    @JvmStatic
    fun remapName(domain: INameMappingService.Domain, name: String): String {
        return when (domain) {
            // FIXME: this is supposed to go from MojMap -> Intermediary
            INameMappingService.Domain.CLASS -> {
                if (srgToNamedCache.contains(name))
                    return srgToNamedCache[name] ?: name
                else if (srgToIntermediaryCache.contains(name))
                    return srgToIntermediaryCache[name] ?: name

                val intermediaryClassName = srgIntermediaryTree.classes.find { it.getName("srg") == name } ?: return name

                val remappedName = if (isDev) {
                    FabricLoader.getInstance().mappingResolver.mapClassName(intermediaryClassName.getName("intermediary"), "named") ?: intermediaryClassName.getName("intermediary")
                } else intermediaryClassName.getName("named")

                if (isDev)
                    srgToNamedCache[name] = remappedName
                else
                    srgToIntermediaryCache[name] = remappedName

                return remappedName
            }

            INameMappingService.Domain.FIELD -> {
                if (srgToNamedCache.contains(name))
                    return srgToNamedCache[name] ?: name
                else if (srgToIntermediaryCache.contains(name))
                    return srgToIntermediaryCache[name] ?: name

                val fieldHoldingClass = srgIntermediaryTree.classes.map {
                    it.fields.any { field -> field.getName("srg") ==  }
                }
            }
        }
    }
}