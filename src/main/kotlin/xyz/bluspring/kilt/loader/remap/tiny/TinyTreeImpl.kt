package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyTree

class TinyTreeImpl(private val metadata: TinyMetadata, private val map: Map<String, ClassDef>, private val classes: Collection<ClassDef>) :
    TinyTree {
    override fun getMetadata(): TinyMetadata {
        return metadata
    }

    override fun getDefaultNamespaceClassMap(): Map<String, ClassDef> {
        return map
    }

    override fun getClasses(): Collection<ClassDef> {
        return classes
    }
}