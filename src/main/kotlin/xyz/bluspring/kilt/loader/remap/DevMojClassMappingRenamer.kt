package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.MappingResolver
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.IRenamer

class DevMojClassMappingRenamer(private val resolver: MappingResolver) : IRenamer {
    override fun rename(value: IMappingFile.IClass): String {
        return resolver.mapClassName("intermediary", value.mapped.replace("/", ".")).replace(".", "/")
    }
}