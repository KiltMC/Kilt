package xyz.bluspring.kilt.loader.asm.coremod

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import xyz.bluspring.kilt.loader.remap.KiltRemapper

open class RemappingMethodInsnNode(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) : MethodInsnNode(opcode,
    KiltRemapper.remapClass(owner, ignoreWorkaround = true),
    name,
    KiltRemapper.remapDescriptor(descriptor), isInterface
) {
    constructor(opcode: Int, owner: String, name: String, descriptor: String) : this(opcode, owner, name, descriptor, opcode == Opcodes.INVOKEINTERFACE)
}