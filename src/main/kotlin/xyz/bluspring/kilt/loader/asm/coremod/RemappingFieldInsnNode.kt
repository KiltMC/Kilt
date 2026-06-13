package xyz.bluspring.kilt.loader.asm.coremod

import org.objectweb.asm.tree.FieldInsnNode
import xyz.bluspring.kilt.loader.remap.KiltRemapper

open class RemappingFieldInsnNode(opcode: Int, owner: String, name: String, descriptor: String) : FieldInsnNode(opcode,
    (owner),
    name,
    KiltRemapper.remapDescriptor(descriptor)
) {
    constructor(original: FieldInsnNode) : this(original.opcode, original.owner, original.name, original.desc)
}
