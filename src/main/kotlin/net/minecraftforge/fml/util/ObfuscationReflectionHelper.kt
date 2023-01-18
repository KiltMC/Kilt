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
            INameMappingService.Domain.CLASS -> {
                val intermediaryClassName = srgIntermediaryTree.classes.find { it.getName("srg") == name } ?: return name

                return if (isDev) {
                    FabricLoader.getInstance().mappingResolver.mapClassName(intermediaryClassName.getName("intermediary"), "named") ?: intermediaryClassName.getName("intermediary")
                } else intermediaryClassName.getName("named")
            }
        }
    }
}