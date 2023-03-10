package xyz.bluspring.kilt.loader.mixin

import org.spongepowered.asm.mixin.extensibility.IRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class SrgMixinRemapper : IRemapper {
    override fun mapMethodName(owner: String?, name: String?, desc: String?): String? {
        if (name == null)
            return null

        return KiltRemapper.methodMappings[name]?.first ?: name
    }

    override fun mapFieldName(owner: String?, name: String?, desc: String?): String? {
        if (name == null)
            return null

        return KiltRemapper.fieldMappings[name]?.first ?: name
    }

    override fun map(typeName: String?): String? {
        if (typeName == null)
            return null

        return KiltRemapper.remapClass(typeName)
    }

    override fun unmap(typeName: String?): String? {
        if (typeName == null)
            return null

        return KiltRemapper.unmapClass(typeName)
    }

    override fun mapDesc(desc: String?): String? {
        if (desc == null)
            return null

        return KiltRemapper.remapDescriptor(desc)
    }

    override fun unmapDesc(desc: String?): String? {
        if (desc == null)
            return null

        return KiltRemapper.remapDescriptor(desc, true)
    }
}