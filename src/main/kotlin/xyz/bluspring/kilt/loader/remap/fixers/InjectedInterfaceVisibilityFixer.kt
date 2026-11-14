package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.remap.KiltRemapper

/**
 * I... don't actually know what this is for...
 */
object InjectedInterfaceVisibilityFixer : GenericClassFixer {
    private val methodsToFix = listOf("getCustomMapData" to KiltRemapper.remapDescriptor("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)"))
    override fun fixClass(classNode: ClassNode) {
        for (methodNode in classNode.methods) {
            for (method in methodsToFix) {
                if (methodNode.name == method.first && methodNode.desc.contains(method.second)) {
                    methodNode.access = Opcodes.ACC_PUBLIC
                }
            }
        }
    }
}
