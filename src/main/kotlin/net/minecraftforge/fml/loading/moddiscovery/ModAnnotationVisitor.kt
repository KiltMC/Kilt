package net.minecraftforge.fml.loading.moddiscovery

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

class ModAnnotationVisitor(
    private val annotations: MutableList<ModAnnotation>,
    private val annotation: ModAnnotation
) : AnnotationVisitor(Opcodes.ASM9) {
    private var isSubAnnotation = false
    private var isArray = false
    private var name: String = ""

    constructor(
        annotations: MutableList<ModAnnotation>,
        annotation: ModAnnotation,
        isSubAnnotation: Boolean
    ) : this(annotations, annotation) {
        this.isSubAnnotation = isSubAnnotation
    }

    constructor(
        annotations: MutableList<ModAnnotation>,
        annotation: ModAnnotation,
        name: String
    ) : this(annotations, annotation) {
        isArray = true
        this.name = name
    }

    override fun visit(name: String, value: Any) {
        annotation.addProperty(name, value)
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        val modAnnotation = annotations[0]
        val child = modAnnotation.addChildAnnotation(name, descriptor)
        annotations.add(0, child)
        return ModAnnotationVisitor(annotations, child, true)
    }

    override fun visitArray(name: String): AnnotationVisitor {
        return ModAnnotationVisitor(annotations, annotation, name)
    }

    override fun visitEnd() {
        if (isArray)
            annotation.endArray()

        if (isSubAnnotation) {
            val child = annotations.removeFirst()
            annotations.add(child)
        }
    }

    override fun visitEnum(name: String, descriptor: String, value: String) {
        annotation.addEnumProperty(name, descriptor, value)
    }
}