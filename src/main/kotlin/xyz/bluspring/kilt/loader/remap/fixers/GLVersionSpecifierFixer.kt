package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import xyz.bluspring.kilt.helpers.DetectedGLVersion
import xyz.bluspring.kilt.loader.remap.MixinHelpers

object GLVersionSpecifierFixer {
    private val GL_VERSION_ANNOTATION = Type.getType(DetectedGLVersion::class.java)

    fun fixClass(classNode: ClassNode) {
        var glMajor = 3
        var glMinor = 2

        for (methodNode in classNode.methods) {
            for (insnNode in methodNode.instructions) {
                if (insnNode is MethodInsnNode) {
                    if (insnNode.owner.startsWith("org/lwjgl/opengl/GL")) {
                        val glClass = insnNode.owner.removePrefix("org/lwjgl/opengl/GL")

                        if (glClass.isNotEmpty() && glClass[0].isDigit() && glClass[1].isDigit()) {
                            val major = glClass[0].digitToInt()
                            val minor = glClass[1].digitToInt()

                            if (major > glMajor) {
                                glMajor = major
                                glMinor = minor
                            } else if (major == glMajor && minor > glMinor) {
                                glMinor = minor
                            }
                        }
                    }
                }
            }
        }

        if (glMajor > 3 || glMinor > 2) {
            val annotations = classNode.visibleAnnotations?.toMutableList() ?: mutableListOf()
            annotations.add(AnnotationNode(Opcodes.ASM9, GL_VERSION_ANNOTATION.descriptor).apply {
                this.values = MixinHelpers.mapToAnnotationValues(mapOf(
                    "majorVersion" to glMajor,
                    "minorVersion" to glMinor
                ))
            })

            classNode.visibleAnnotations = annotations
        }
    }
}
