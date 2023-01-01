package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper
import net.fabricmc.mapping.tree.FieldDef
import net.fabricmc.tinyremapper.TinyRemapper
import net.minecraftforge.srgutils.IMappingFile
import java.nio.file.Path

class SrgFieldDef(private val mappedField: IMappingFile.IField, private val srgClass: SrgClassDef) : FieldDef {
    override fun getName(namespace: String?): String {
        return mappedField.mapped
    }

    override fun getRawName(namespace: String?): String {
        return mappedField.mapped
    }

    override fun getComment(): String? {
        return null
    }

    override fun getDescriptor(namespace: String?): String {
        if (mappedField.mappedDescriptor != null)
            return mappedField.mappedDescriptor!!

        val name = srgClass.mappedClass.mapped
        val mappedClass = srgClass.mojMappings.getClass(name)
        val mappedField = mappedClass.getField(mappedField.mapped)

        return mappedField!!.descriptor!!
    }
}