package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.tinyremapper.api.TrRemapper
import net.minecraftforge.srgutils.IMappingFile
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper

class WrappedTinyRemapper(private val delegate: KiltEnhancedRemapper, private val mapping: IMappingFile) : TrRemapper() {
    override fun mapMethodNamePrefixDesc(
        owner: String,
        name: String,
        descPrefix: String
    ): String? {
        return delegate.mapMethodNamePrefixDesc(owner, name, descPrefix)
    }

    override fun mapMethodArg(
        methodOwner: String?,
        methodName: String?,
        methodDesc: String?,
        lvIndex: Int,
        name: String?
    ): String? {
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

    override fun mapAnnotationAttributeName(
        annotationDesc: String?,
        name: String?,
        attributeDesc: String?
    ): String? {
        return name
    }

    override fun map(internalName: String): String {
        return delegate.map(internalName)
    }

    override fun mapDesc(descriptor: String?): String? {
        return delegate.mapDesc(descriptor)
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return delegate.mapFieldName(owner, name, descriptor)
    }

    override fun mapInnerClassName(name: String?, ownerName: String?, innerName: String?): String? {
        return delegate.mapInnerClassName(name, ownerName, innerName)
    }

    override fun mapInvokeDynamicMethodName(name: String?, descriptor: String?): String? {
        return delegate.mapInvokeDynamicMethodName(name, descriptor)
    }

    override fun mapMethodDesc(methodDescriptor: String?): String? {
        return delegate.mapMethodDesc(methodDescriptor)
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return delegate.mapMethodName(owner, name, descriptor)
    }

    override fun mapModuleName(name: String?): String? {
        return delegate.mapModuleName(name)
    }

    override fun mapPackageName(name: String?): String? {
        return delegate.mapPackageName(name)
    }

    override fun mapRecordComponentName(owner: String?, name: String?, descriptor: String?): String? {
        return delegate.mapRecordComponentName(owner, name, descriptor)
    }

    override fun mapSignature(signature: String?, typeSignature: Boolean): String? {
        return delegate.mapSignature(signature, typeSignature)
    }

    override fun mapType(internalName: String?): String? {
        return delegate.mapType(internalName)
    }

    override fun mapTypes(internalNames: Array<out String?>?): Array<out String?>? {
        return delegate.mapTypes(internalNames)
    }

    override fun mapValue(value: Any?): Any? {
        return delegate.mapValue(value)
    }

    override fun mapAnnotationAttributeName(descriptor: String?, name: String?): String? {
        return delegate.mapAnnotationAttributeName(descriptor, name)
    }
}