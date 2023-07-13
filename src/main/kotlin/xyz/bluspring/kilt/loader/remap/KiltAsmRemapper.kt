package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter

class KiltAsmRemapper(
    private val fieldMappings: Map<String, Pair<String, String>>,
    private val methodMappings: Map<String, Pair<String, String>>
) : Remapper() {
    // Pair of owner and name + descriptor
    val possibleFailures = mutableListOf<Pair<String, String>>()

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
        val mappedPair = fieldMappings[name] ?: return name

        if (KiltRemapper.remapDescriptor(descriptor) != mappedPair.second)
            return name

        return mappedPair.first
    }

    override fun mapRecordComponentName(owner: String, name: String, descriptor: String): String {
        return mapFieldName(owner, name, descriptor)
    }

    private val ownerWhitelist = listOf(
        "xyz/bluspring/kilt/loader/",
        "java/",
        "kotlin/",
        "org/jetbrains/",
        "javax/",
        "kotlinx/",
        "net/minecraftforge/fml/",
        "io/netty/",
        "org/apache/"
    )

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        val mappedPair = methodMappings[name] ?: return name
        val mapped = mappedPair.first

        val remappedDescriptor = KiltRemapper.remapDescriptor(descriptor)
        val remappedDescriptorNoEnd = remappedDescriptor.replaceAfter(")", "")

        if (remappedDescriptorNoEnd != mappedPair.second.replaceAfter(")", "")) {
            // just throw it over if it's SRG-mapped.
            if ((name.startsWith("m_") || name.startsWith("f_")) && name.endsWith("_"))
                return mapped

            return name
        }

        if ((!name.startsWith("m_") && !name.startsWith("f_")) && !name.endsWith("_")) {
            // Ignore these owner types specifically, because they are confirmed to be safe.
            if (ownerWhitelist.any { owner.startsWith(it) })
                return name

            // Try to see if these mappings can be used to help
            val srgClassTree = KiltRemapper.srgIntermediaryTree.classes.firstOrNull { it.getName("searge") == owner }

            if (srgClassTree != null) {
                val matchingMethod = srgClassTree.methods.firstOrNull { it.getName("searge") == name && it.getDescriptor("intermediary").replaceAfter(")", "") == remappedDescriptorNoEnd }

                if (matchingMethod != null) {
                    val intermediaryName = matchingMethod.getName("intermediary")

                    return if (KiltRemapper.useNamed)
                        FabricLoader.getInstance().mappingResolver.mapMethodName("intermediary", srgClassTree.getName("intermediary"), intermediaryName, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))
                    else
                        intermediaryName
                }
            }

            // Because of the way the remapper works, it remaps every name sharing the same f_num_ and m_num_ names
            // without checking the owners, because it functions under the assumption that every intermediate name
            // is of the same mapped name. However, that assumption fails when it reaches a non-intermediate name,
            // because Intermediary generates intermediate names for almost everything, while TSRG does not.
            // As a result, that assumption fails, and *will* end up remapping names that are completely unrelated.
            // This error never occurs in development, but will occur in production environments.
            // Therefore, this information needs to be stored in the mod in order to make sure the run process goes
            // as smoothly as possible.
            possibleFailures.add(Pair(owner, "$name:$descriptor"))
            return name
        }

        return mapped
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

    override fun mapInnerClassName(name: String, ownerName: String?, innerName: String?): String {
        return remapClass(name)
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        return methodMappings[name]?.first ?: name
    }
}