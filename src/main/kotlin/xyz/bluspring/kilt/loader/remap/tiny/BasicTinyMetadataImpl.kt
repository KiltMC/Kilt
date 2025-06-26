package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.mapping.reader.v2.TinyMetadata

class BasicTinyMetadataImpl(private val namespaces: List<String>) : TinyMetadata {
    override fun getMajorVersion(): Int {
        return 2
    }

    override fun getMinorVersion(): Int {
        return 0
    }

    override fun getNamespaces(): List<String> {
        return namespaces
    }

    override fun getProperties(): Map<String, String?> {
        return mapOf()
    }
}