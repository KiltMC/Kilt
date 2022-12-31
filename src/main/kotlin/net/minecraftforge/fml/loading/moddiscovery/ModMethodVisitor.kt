package net.minecraftforge.fml.loading.moddiscovery

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.annotation.ElementType

class ModMethodVisitor(private val name: String, private val descriptor: String, private val annotations: MutableList<ModAnnotation>) : MethodVisitor(Opcodes.ASM9) {
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val annotation = ModAnnotation(ElementType.METHOD, Type.getType(descriptor), name + this.descriptor)
        annotations.add(0, annotation)
        return ModAnnotationVisitor(annotations, annotation)
    }
}