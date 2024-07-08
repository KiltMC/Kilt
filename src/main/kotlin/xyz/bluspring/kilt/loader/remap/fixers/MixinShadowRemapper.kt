package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object MixinShadowRemapper {
    fun remapClass(classNode: ClassNode) {
        val remappedFields = mutableMapOf<String, String>()
        val remappedMethods = mutableMapOf<String, String>()

        // Collect all shadow fields
        for (field in classNode.fields) {
            if (field.visibleAnnotations == null)
                continue

            if (field.visibleAnnotations.none { it.desc.contains("org/spongepowered/asm/mixin/Shadow") })
                continue

            val remapped = KiltRemapper.srgMappedFields[field.name]?.second ?: continue
            remappedFields[field.name] = remapped
            field.name = remapped
        }

        // Collect all shadow methods
        for (method in classNode.methods) {
            if (method.visibleAnnotations == null)
                continue

            if (method.visibleAnnotations.none { it.desc.contains("org/spongepowered/asm/mixin/Shadow") })
                continue

            val remapped = KiltRemapper.srgMappedMethods[method.name]?.get(classNode.name.replace(".", "/")) ?: KiltRemapper.srgMappedMethods[method.name]?.values?.firstOrNull() ?: continue
            remappedMethods[method.name] = remapped
            method.name = remapped
        }

        // Second pass, go through all the methods and point everything to the remapped shadows
        for (method in classNode.methods) {
            for (insnNode in method.instructions) {
                if (insnNode is FieldInsnNode) {
                    val remapped = remappedFields[insnNode.name] ?: continue
                    insnNode.name = remapped
                } else if (insnNode is MethodInsnNode) {
                    val remapped = remappedMethods[insnNode.name] ?: continue
                    insnNode.name = remapped
                }
            }
        }
    }
}