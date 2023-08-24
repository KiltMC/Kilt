package net.minecraftforge.fml.util

import cpw.mods.modlauncher.api.INameMappingService
import net.fabricmc.loader.impl.FabricLoaderImpl
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object ObfuscationReflectionHelper {
    private val srgIntermediaryTree = KiltRemapper.srgIntermediaryTree
    private val srgMappedFields =
        srgIntermediaryTree.classes.flatMap { it.fields }.associateBy { it.getName("searge") }
    private val srgMappedMethods =
        srgIntermediaryTree.classes.flatMap { it.methods }.associateBy { it.getName("searge") }
    private val mappingEnvironment = FabricLoaderImpl.INSTANCE

    @JvmStatic
    fun remapName(domain: INameMappingService.Domain, name: String): String {
        // TODO: Make this remap from MojMap to Intermediary.
        // We might have to package MojMap alongside Kilt, however that would very much
        // violate the redistribution part of MojMaps.
        return name
    }

    class UnableToAccessFieldException private constructor(e: Exception) : RuntimeException(e)

    class UnableToFindFieldException private constructor(e: Exception) : RuntimeException(e)

    class UnableToFindMethodException(failed: Throwable?) : RuntimeException(failed)

    class UnknownConstructorException(message: String?) : RuntimeException(message)
}