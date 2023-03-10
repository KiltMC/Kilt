package xyz.bluspring.kilt.loader.mixin

import org.spongepowered.asm.mixin.extensibility.IRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class SrgMixinRemapper : IRemapper {
    override fun mapMethodName(owner: String, name: String, desc: String): String {
        return KiltRemapper.methodMappings[name]?.first ?: name
    }

    override fun mapFieldName(owner: String, name: String, desc: String): String {
        return KiltRemapper.fieldMappings[name]?.first ?: name
    }

    override fun map(typeName: String): String {
        return KiltRemapper.remapClass(typeName)
    }

    override fun unmap(typeName: String): String {
        return KiltRemapper.unmapClass(typeName)
    }

    override fun mapDesc(desc: String): String {
        return KiltRemapper.remapDescriptor(desc)
    }

    override fun unmapDesc(desc: String): String {
        return KiltRemapper.remapDescriptor(desc, true)
    }
}