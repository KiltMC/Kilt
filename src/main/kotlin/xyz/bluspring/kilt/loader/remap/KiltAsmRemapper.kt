package xyz.bluspring.kilt.loader.remap

import net.fabricmc.mapping.tree.TinyTree
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter

class KiltAsmRemapper(
    private val fieldMappings: Map<String, Pair<String, String>>,
    private val methodMappings: Map<String, Pair<String, String>>
) : Remapper() {
    private fun remapClass(name: String): String {
        return KiltRemapper.remapClass(name)
    }

    private fun remapDescriptor(descriptor: String): String {
        return KiltRemapper.remapDescriptor(descriptor)
    }

    private fun remapSignature(signature: String): String {
        val parser = SignatureReader(signature)
        val writer = object : SignatureWriter() {
            override fun visitClassType(name: String?) {
                super.visitClassType(if (name != null) remapClass(name) else null)
            }

            override fun visitInnerClassType(name: String?) {
                super.visitInnerClassType(if (name != null) remapClass(name) else null)
            }

            override fun visitTypeVariable(name: String?) {
                super.visitTypeVariable(if (name != null) remapClass(name) else null)
            }
        }

        parser.accept(writer)

        return writer.toString()
    }

    override fun map(internalName: String): String {
        return remapClass(internalName)
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return fieldMappings[name]?.first ?: name
    }

    override fun mapRecordComponentName(owner: String, name: String, descriptor: String): String {
        return mapFieldName(owner, name, descriptor)
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return methodMappings[name]?.first ?: name
    }

    override fun mapMethodDesc(methodDescriptor: String): String {
        return remapDescriptor(methodDescriptor)
    }

    override fun mapDesc(descriptor: String): String {
        return remapDescriptor(descriptor)
    }

    override fun mapSignature(signature: String?, typeSignature: Boolean): String? {
        if (signature == null) // why is this a thing
            return null

        return remapSignature(signature)
    }

    override fun mapAnnotationAttributeName(descriptor: String, name: String): String {
        return methodMappings[name]?.first ?: name
    }

    override fun mapInnerClassName(name: String, ownerName: String, innerName: String): String {
        return remapClass(name)
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        return methodMappings[name]?.first ?: name
    }
}