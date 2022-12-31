package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.LocalVariableDef
import net.fabricmc.mapping.tree.MethodDef
import net.fabricmc.mapping.tree.ParameterDef
import net.minecraftforge.srgutils.IMappingFile

class SrgMethodDef(private val mappedMethod: IMappingFile.IMethod) : MethodDef {
    override fun getName(namespace: String?): String {
        return mappedMethod.mapped
    }

    override fun getRawName(namespace: String?): String {
        return mappedMethod.mapped
    }

    override fun getComment(): String? {
        return null
    }

    override fun getDescriptor(namespace: String?): String {
        return mappedMethod.mappedDescriptor
    }

    override fun getParameters(): MutableCollection<ParameterDef> {
        return mappedMethod.parameters.map(::SrgParameterDef).toMutableList()
    }

    override fun getLocalVariables(): MutableCollection<LocalVariableDef> {
        return mutableListOf()
    }
}