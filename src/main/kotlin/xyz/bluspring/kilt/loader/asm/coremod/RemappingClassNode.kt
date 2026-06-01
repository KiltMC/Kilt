package xyz.bluspring.kilt.loader.asm.coremod

import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class RemappingClassNode(api: Int = Opcodes.ASM9, private val remapper: KiltEnhancedRemapper) : ClassNode(api) {
    init {
        this.fields = TransformingList(this.fields) {
            FieldNode(Opcodes.ASM9, it.access, remapper.mapFieldName(this.name, it.name, it.desc), KiltRemapper.remapDescriptor(it.desc), it.signature, it.value)
        }

        this.methods = TransformingList(this.methods) {
            MethodNode(Opcodes.ASM9, it.access, remapper.mapMethodName(this.name, it.name, it.desc), KiltRemapper.remapDescriptor(it.desc), it.signature, it.exceptions.toTypedArray())
                .apply {
                    RemappingInsnList(it.instructions)
                }
        }
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        return super.visitField(access, remapper.mapFieldName(this.name, name, descriptor), descriptor, signature, value)
    }
}
