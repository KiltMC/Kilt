package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.*
import java.lang.annotation.ElementType

class ModClassVisitor : ClassVisitor(Opcodes.ASM9) {
    private val annotations = mutableListOf<ModAnnotation>()
    private var asmSuperType: Type? = null
    private var asmType: Type? = null
    private var interfaces: Set<Type>? = null

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val annotation = ModAnnotation(ElementType.TYPE, Type.getType(descriptor), asmType!!.className)
        annotations.add(0, annotation)

        return ModAnnotationVisitor(annotations, annotation)
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String?,
        interfaces: Array<out String>
    ) {
        asmType = Type.getObjectType(name)
        asmSuperType = if (superName != null && superName.isNotBlank()) Type.getObjectType(superName) else null
        this.interfaces = interfaces.map(Type::getObjectType).toSet()
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String,
        value: Any
    ): FieldVisitor {
        return ModFieldVisitor(name, annotations)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String,
        exceptions: Array<out String>
    ): MethodVisitor {
        return ModMethodVisitor(name, descriptor, annotations)
    }

    fun buildData(classes: MutableSet<ModFileScanData.ClassData>, annotations: MutableSet<ModFileScanData.AnnotationData>) {
        classes.add(ModFileScanData.ClassData(asmType, asmSuperType, interfaces))
        annotations.addAll(
            this.annotations
                .filter { ModFileScanData.interestingAnnotations().test(it.getASMType()) }
                .map { ModAnnotation.fromModAnnotation(asmType!!, it) }
        )
    }
}