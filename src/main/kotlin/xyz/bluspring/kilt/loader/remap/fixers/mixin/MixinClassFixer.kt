package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.fixers.GenericClassFixer

interface MixinClassFixer : GenericClassFixer {
    fun fixClass(classNode: ClassNode, remapper: KiltEnhancedRemapper) {
        this.fixClass(classNode)
    }

    override fun fixClass(classNode: ClassNode) {
    }
}
