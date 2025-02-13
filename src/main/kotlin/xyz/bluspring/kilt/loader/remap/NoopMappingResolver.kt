package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.MappingResolver

class NoopMappingResolver : MappingResolver {
    override fun getNamespaces(): MutableCollection<String> {
        return mutableListOf("intermediary")
    }

    override fun getCurrentRuntimeNamespace(): String {
        return FabricLoader.getInstance().mappingResolver.currentRuntimeNamespace
    }

    override fun mapClassName(namespace: String, className: String): String {
        return className
    }

    override fun unmapClassName(targetNamespace: String?, className: String): String {
        return className
    }

    override fun mapFieldName(namespace: String?, owner: String?, name: String, descriptor: String?): String {
        return name
    }

    override fun mapMethodName(namespace: String?, owner: String?, name: String, descriptor: String?): String {
        return name
    }
}