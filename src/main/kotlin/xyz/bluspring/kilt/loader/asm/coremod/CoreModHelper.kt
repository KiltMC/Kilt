package xyz.bluspring.kilt.loader.asm.coremod

import xyz.bluspring.kilt.loader.remap.KiltRemapper

object CoreModHelper {
    @JvmStatic
    fun remapClass(name: String): String {
        return KiltRemapper.remapClass(name, ignoreWorkaround = true)
    }

    @JvmStatic
    fun remapDescriptor(descriptor: String): String {
        return KiltRemapper.remapDescriptor(descriptor)
    }
}