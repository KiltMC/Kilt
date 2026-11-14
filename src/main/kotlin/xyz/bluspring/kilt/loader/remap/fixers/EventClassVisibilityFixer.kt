package xyz.bluspring.kilt.loader.remap.fixers

import net.neoforged.bus.api.SubscribeEvent
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * Automatically makes all classes consisting of @[SubscribeEvent] public, because otherwise NeoForge can't access it.
 *
 * This might've actually been exclusive to Forge, but I'm not gonna break it.
 */
object EventClassVisibilityFixer : GenericClassFixer {
    override fun fixClass(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (method.visibleAnnotations != null && method.visibleAnnotations.any { it.desc.contains("SubscribeEvent") })
                method.access = (method.access and Opcodes.ACC_PRIVATE.inv() and Opcodes.ACC_PROTECTED.inv()) or Opcodes.ACC_PUBLIC
        }

        // Mark class as public
        classNode.access = (classNode.access and Opcodes.ACC_PRIVATE.inv() and Opcodes.ACC_PROTECTED.inv()) or Opcodes.ACC_PUBLIC
    }
}
