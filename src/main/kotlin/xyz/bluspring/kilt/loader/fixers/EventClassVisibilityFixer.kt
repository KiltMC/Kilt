package xyz.bluspring.kilt.loader.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object EventClassVisibilityFixer {
    fun fixClass(classNode: ClassNode) {
        if (classNode.methods.none { m -> m.visibleAnnotations != null && m.visibleAnnotations.any { it.desc.contains("SubscribeEvent") } })
            return

        classNode.access = (classNode.access and Opcodes.ACC_PRIVATE.inv() and Opcodes.ACC_PROTECTED.inv()) or Opcodes.ACC_PUBLIC
    }
}