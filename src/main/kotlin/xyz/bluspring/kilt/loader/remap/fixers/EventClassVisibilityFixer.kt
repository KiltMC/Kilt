package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object EventClassVisibilityFixer {
    fun fixClass(classNode: ClassNode) {
        if (classNode.methods.none { m -> m.visibleAnnotations != null && m.visibleAnnotations.any { it.desc.contains("SubscribeEvent") } })
            return

        // Mark class as public
        classNode.access = (classNode.access and Opcodes.ACC_PRIVATE.inv() and Opcodes.ACC_PROTECTED.inv()) or Opcodes.ACC_PUBLIC
    }
}