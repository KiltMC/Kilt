package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.tree.ClassNode

interface GenericClassFixer {
    fun fixClass(classNode: ClassNode)
}
