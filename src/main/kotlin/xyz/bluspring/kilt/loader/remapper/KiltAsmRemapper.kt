package xyz.bluspring.kilt.loader.remapper

import net.fabricmc.mapping.tree.TinyTree
import org.objectweb.asm.commons.Remapper

class KiltAsmRemapper(private val tree: TinyTree, private val from: String, private val to: String) : Remapper() {
    override fun map(internalName: String?): String? {
        return tree.classes.firstOrNull { it.getName(from) == internalName }?.getName(to)
    }

    override fun mapFieldName(owner: String?, name: String?, descriptor: String?): String? {
        return mapFieldName(owner, name)
    }

    private fun mapFieldName(owner: String?, name: String?): String? {
        val ownerClass = tree.classes.firstOrNull { it.getName(from) == owner } ?: return null
        val field = ownerClass.fields.firstOrNull { it.getName(from) == name } ?: return null

        return field.getName(to)
    }

    override fun mapMethodName(owner: String?, name: String?, descriptor: String?): String? {
        val ownerClass = tree.classes.firstOrNull { it.getName(from) == owner } ?: return null
        val method = ownerClass.methods.firstOrNull { it.getName(from) == name && it.getDescriptor(from) == descriptor } ?: return null

        return method.getName(to)
    }
}