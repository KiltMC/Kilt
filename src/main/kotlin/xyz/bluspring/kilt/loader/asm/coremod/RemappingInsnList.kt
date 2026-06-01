package xyz.bluspring.kilt.loader.asm.coremod

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode

class RemappingInsnList(backing: InsnList) : InsnList() {
    init {
        this.insert(backing)
    }

    private fun remapNode(insnNode: AbstractInsnNode): AbstractInsnNode {
        return when (insnNode) {
            is FieldInsnNode -> RemappingFieldInsnNode(insnNode)
            is MethodInsnNode -> RemappingMethodInsnNode(insnNode)
            else -> insnNode
        }
    }

    override fun set(oldInsnNode: AbstractInsnNode, newInsnNode: AbstractInsnNode) {
        super.set(oldInsnNode, remapNode(newInsnNode))
    }

    override fun add(insnNode: AbstractInsnNode) {
        super.add(remapNode(insnNode))
    }

    override fun add(insnList: InsnList) {
        for (node in insnList) {
            this.add(node)
        }
    }

    override fun insert(insnNode: AbstractInsnNode) {
        super.insert(remapNode(insnNode))
    }

    override fun insert(previousInsn: AbstractInsnNode, insnList: InsnList) {
        val list = RemappingInsnList(InsnList())
        for (node in insnList) {
            list.add(node)
        }

        super.insert(previousInsn, list)
    }

    override fun insert(previousInsn: AbstractInsnNode, insnNode: AbstractInsnNode) {
        super.insert(previousInsn, remapNode(insnNode))
    }

    override fun insertBefore(nextInsn: AbstractInsnNode, insnList: InsnList) {
        val list = RemappingInsnList(InsnList())
        for (node in insnList) {
            list.add(node)
        }

        super.insertBefore(nextInsn, list)
    }

    override fun insertBefore(nextInsn: AbstractInsnNode, insnNode: AbstractInsnNode) {
        super.insertBefore(nextInsn, remapNode(insnNode))
    }

    override fun insert(insnList: InsnList) {
        for (node in insnList) {
            this.insert(node)
        }
    }
}
