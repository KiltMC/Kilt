package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.ParameterDef
import net.minecraftforge.srgutils.IMappingFile

class SrgParameterDef(private val mappedParameter: IMappingFile.IParameter, val methodDef: SrgMethodDef) : ParameterDef {
    override fun getName(namespace: String?): String {
        return mappedParameter.mapped
    }

    override fun getRawName(namespace: String?): String {
        return mappedParameter.mapped
    }

    override fun getComment(): String? {
        return null
    }

    override fun getLocalVariableIndex(): Int {
        return mappedParameter.index
    }
}