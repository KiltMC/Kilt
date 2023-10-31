package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

object CompatibleCapabilityWorkaroundFixer {
    private val mappingResolver = FabricLoader.getInstance().mappingResolver
    private val workaroundClasses = listOf(
        mappingResolver.mapClassName("intermediary", "net.minecraft.class_1799").replace(".", "/")
    )

    fun fixClass(classNode: ClassNode) {
        for (method in classNode.methods) {
            val newNodeMap = mutableMapOf<AbstractInsnNode, AbstractInsnNode>()

            for (insnNode in method.instructions) {
                // Target virtual invokes specifically
                if (insnNode is MethodInsnNode && insnNode.opcode == Opcodes.INVOKEVIRTUAL) {
                    if (!workaroundClasses.contains(insnNode.owner))
                        continue

                    if (insnNode.name != "areCapsCompatible")
                        continue

                    val node = MethodInsnNode(insnNode.opcode, insnNode.owner, insnNode.name, "(Lnet/minecraftforge/common/capabilities/ICapabilityProviderImpl;)Z")
                    newNodeMap[insnNode] = node
                }
            }

            if (newNodeMap.isNotEmpty()) {
                for ((oldNode, newNode) in newNodeMap) {
                    method.instructions.set(oldNode, newNode)
                }
            }
        }
    }
}