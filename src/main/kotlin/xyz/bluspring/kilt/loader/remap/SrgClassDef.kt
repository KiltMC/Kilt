package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.*
import net.minecraftforge.srgutils.IMappingFile

class SrgClassDef(private val mappedClass: IMappingFile.IClass) : ClassDef {
    override fun getName(namespace: String?): String {
        return mappedClass.mapped
    }

    override fun getRawName(namespace: String?): String {
        return mappedClass.mapped
    }

    override fun getComment(): String? {
        return null
    }

    override fun getMethods(): MutableCollection<MethodDef> {
        return mappedClass.methods.map(::SrgMethodDef).toMutableList()
    }

    override fun getFields(): MutableCollection<FieldDef> {
        return mappedClass.fields.map {
            SrgFieldDef(it, this)
        }.toMutableList()
    }
}