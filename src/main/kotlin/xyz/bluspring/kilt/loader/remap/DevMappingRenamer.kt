package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.IRenamer

class DevMappingRenamer : IRenamer {
    private val resolver = FabricLoader.getInstance().mappingResolver

    override fun rename(value: IMappingFile.IField): String {
        return resolver.mapFieldName("intermediary", value.parent.mapped.replace("/", "."), value.mapped, value.mappedDescriptor)
    }

    override fun rename(value: IMappingFile.IMethod): String {
        return resolver.mapMethodName("intermediary", value.parent.mapped.replace("/", "."), value.mapped, value.mappedDescriptor)
    }

    override fun rename(value: IMappingFile.IClass): String {
        return resolver.mapClassName("intermediary", value.mapped.replace("/", ".")).replace(".", "/")
    }
}