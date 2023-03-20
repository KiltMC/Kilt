package net.minecraftforge.fml.loading

import java.util.Optional

object JarVersionLookupHandler {
    @JvmStatic
    fun getImplementationVersion(clazz: Class<*>): Optional<String> {
        return Optional.empty()
    }

    @JvmStatic
    fun getSpecificationVersion(clazz: Class<*>): Optional<String> {
        return Optional.empty()
    }
}