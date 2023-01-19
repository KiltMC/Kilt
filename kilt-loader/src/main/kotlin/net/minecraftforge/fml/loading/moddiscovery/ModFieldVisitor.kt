package net.minecraftforge.fml.loading.moddiscovery

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.annotation.ElementType

class ModFieldVisitor(private val name: String, private val annotations: MutableList<ModAnnotation>) : FieldVisitor(Opcodes.ASM9) {
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val annotation = ModAnnotation(ElementType.FIELD, Type.getType(descriptor), name)
        annotations.add(0, annotation)
        return ModAnnotationVisitor(annotations, annotation)
    }
}