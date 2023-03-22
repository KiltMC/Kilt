package net.minecraftforge.fml.loading

import net.minecraftforge.versions.forge.ForgeVersion
import xyz.bluspring.kilt.loader.KiltLoader
import java.util.Optional

object JarVersionLookupHandler {
    private val forgeVersionClass = ForgeVersion::class.java

    @JvmStatic
    fun getImplementationVersion(clazz: Class<*>): Optional<String> {
        return Optional.empty()
    }

    @JvmStatic
    fun getSpecificationVersion(clazz: Class<*>): Optional<String> {
        if (clazz == forgeVersionClass)
            return Optional.of(KiltLoader.SUPPORTED_FORGE_SPEC_VERSION.toString())

        return Optional.empty()
    }

    @JvmStatic
    fun getImplementationTitle(clazz: Class<*>): Optional<String> {
        return Optional.empty()
    }
}