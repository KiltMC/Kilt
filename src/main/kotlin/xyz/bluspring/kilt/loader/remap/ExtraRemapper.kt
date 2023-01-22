package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.tinyremapper.MemberInstance
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.api.TrClass
import net.fabricmc.tinyremapper.api.TrMethod
import net.fabricmc.tinyremapper.api.TrRemapper
import org.objectweb.asm.Type

// This remapper is used to essentially work around a problem with Tiny
// where it doesn't seem to properly remap certain fields and methods.
class ExtraRemapper(private val tree: TinyTree, private val from: String, private val to: String) : TrRemapper() {
    lateinit var remapper: TinyRemapper

    override fun mapAnnotationAttributeName(annotationDesc: String?, name: String?, attributeDesc: String?): String {
        val annotationClass: String = Type.getType(annotationDesc).internalName

        return if (attributeDesc == null) {
            mapMethodNamePrefixDesc(annotationClass, name, "()")
        } else {
            mapMethodName(annotationClass, name, "()$attributeDesc")
        }
    }

    override fun mapMethodNamePrefixDesc(owner: String?, name: String?, descPrefix: String?): String {
        val cls = getClass(owner) ?: return name!!

        val members: Collection<TrMethod> = cls.resolveMethods(name, descPrefix, true, null, null)
        val member: MemberInstance? = if (members.size == 1) members.iterator().next() as MemberInstance else null

        return if (member != null && member.newName != null) {
            member.newName
        } else name!!
    }

    override fun mapMethodArg(
        methodOwner: String?,
        methodName: String?,
        methodDesc: String?,
        lvIndex: Int,
        name: String?
    ): String? {
        // should be handled by Tiny already. I hope.
        return name
    }

    override fun mapMethodVar(
        methodOwner: String?,
        methodName: String?,
        methodDesc: String?,
        lvIndex: Int,
        startOpIdx: Int,
        asmIndex: Int,
        name: String?
    ): String? {
        return name
    }

    override fun mapFieldName(owner: String?, name: String?, descriptor: String?): String {
        /*val cls = getClass(owner)
            ?: return tree.classes
                .firstOrNull { it.getName(from) == owner }?.fields
                ?.firstOrNull { it.getName(from) == name }
                ?.getName(to) ?: name!!*/

        return mapFieldName(name)
    }

    private fun mapFieldName(name: String?): String {
        return tree.classes.find {
            it.fields.any { field ->
                field.getName(from) == name
            }
        }?.fields?.first { it.getName(from) == name }?.getName(to) ?: name!!
    }

    override fun mapMethodName(owner: String?, name: String?, descriptor: String?): String {
        if (descriptor?.startsWith("(") == false)
            return mapFieldName(name)

        return tree.classes.find {
            it.methods.any { method ->
                method.getName(from) == name
            }
        }?.methods?.first { it.getName(from) == name }?.getName(to) ?: name!!
    }

    private fun getClass(name: String?): TrClass? {
        return remapper.environment.getClass(name)
    }
}