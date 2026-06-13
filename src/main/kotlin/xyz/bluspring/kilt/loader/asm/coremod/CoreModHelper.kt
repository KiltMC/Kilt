package xyz.bluspring.kilt.loader.asm.coremod

import xyz.bluspring.kilt.loader.remap.KiltRemapper

object CoreModHelper {
    @JvmStatic
    fun remapClass(name: String): String {
        return (name)
    }

    @JvmStatic
    fun remapDescriptor(descriptor: String): String {
        return KiltRemapper.remapDescriptor(descriptor)
    }
}
