package xyz.bluspring.kilt.loader.remap

import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.srgutils.IMappingFile
import java.util.function.Consumer

class KiltEnhancedRemapper(provider: ClassProvider, file: IMappingFile, log: Consumer<String>) : EnhancedRemapper(provider, file, log) {
    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        if (name.startsWith("m_") && name.endsWith("_")) {
            return KiltRemapper.srgMappedMethods[name]?.second ?: super.mapMethodName(owner, name, descriptor)
        }

        return super.mapMethodName(owner, name, descriptor)
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (name.startsWith("f_") && name.endsWith("_")) {
            return KiltRemapper.srgMappedFields[name]?.second ?: super.mapFieldName(owner, name, descriptor)
        }

        return super.mapFieldName(owner, name, descriptor)
    }
}