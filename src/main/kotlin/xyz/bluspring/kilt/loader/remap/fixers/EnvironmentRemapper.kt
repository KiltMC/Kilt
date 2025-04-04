package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.api.Environment
import net.fabricmc.api.EnvironmentInterface
import net.fabricmc.api.EnvironmentInterfaces
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.api.distmarker.OnlyIns
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifications
import xyz.bluspring.kilt.util.DistUtil
import java.util.function.Consumer

object EnvironmentRemapper {
    private val ONLYIN_TYPE = Type.getType(OnlyIn::class.java)
    private val ONLYINS_TYPE = Type.getType(OnlyIns::class.java)
    private val ENVIRONMENT_TYPE = Type.getType(Environment::class.java)
    private val ENVIRONMENT_INTERFACE_TYPE = Type.getType(EnvironmentInterface::class.java)
    private val ENVIRONMENT_INTERFACES_TYPE = Type.getType(EnvironmentInterfaces::class.java)

    fun remapClass(classNode: ClassNode) {
        // Class annotation replacements
        if (classNode.visibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
            replaceAnnotations(classNode.visibleAnnotations) { classNode.visibleAnnotations = it }
        }

        if (classNode.invisibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
            replaceAnnotations(classNode.invisibleAnnotations) { classNode.invisibleAnnotations = it }
        }

        // Field annotation replacements
        for (fieldNode in classNode.fields) {
            if (fieldNode.visibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
                replaceAnnotations(fieldNode.visibleAnnotations) { fieldNode.visibleAnnotations = it }
            }

            if (fieldNode.invisibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
                replaceAnnotations(fieldNode.invisibleAnnotations) { fieldNode.invisibleAnnotations = it }
            }
        }

        // Method annotation replacements
        for (methodNode in classNode.methods) {
            if (methodNode.visibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
                replaceAnnotations(methodNode.visibleAnnotations) { methodNode.visibleAnnotations = it }
            }

            if (methodNode.invisibleAnnotations?.any { it.desc == ONLYIN_TYPE.descriptor } == true) {
                replaceAnnotations(methodNode.invisibleAnnotations) { methodNode.invisibleAnnotations = it }
            }
        }
    }

    private fun replaceAnnotations(annotations: List<AnnotationNode>, setter: Consumer<List<AnnotationNode>>) {
        val replaceable = annotations.filter { it.desc != ONLYIN_TYPE.descriptor && it.desc != ONLYINS_TYPE.descriptor }.toMutableList()

        for (node in annotations) {
            if (node.desc == ONLYINS_TYPE.descriptor) {
                val values = KiltMixinModifications.annotationValuesToMap(node.values)["value"]
                val newValues = mutableListOf<AnnotationNode>()
                if (values is List<*>) {
                    for (annotation in values) {
                        if (annotation !is AnnotationNode)
                            continue

                        if (annotation.desc != ONLYIN_TYPE.descriptor)
                            continue

                        val aValues = KiltMixinModifications.annotationValuesToMap(annotation.values)

                        // I think we can safely assume this is okay
                        newValues.add(AnnotationNode(Opcodes.ASM9, ENVIRONMENT_INTERFACE_TYPE.descriptor).apply {
                            this.values = KiltMixinModifications.mapToAnnotationValues(mapOf(
                                "value" to DistUtil.distToEnvType(aValues["value"] as Dist),
                                "itf" to aValues["_interface"] as Class<*>
                            ))
                        })
                    }
                }

                replaceable.add(AnnotationNode(Opcodes.ASM9, ENVIRONMENT_INTERFACES_TYPE.descriptor).apply {
                    this.values = KiltMixinModifications.mapToAnnotationValues(mapOf(
                        "value" to newValues
                    ))
                })
            } else if (node.desc == ONLYIN_TYPE.descriptor) {
                val values = KiltMixinModifications.annotationValuesToMap(node.values)
                val value = values["value"] as Dist
                val itf = values["_interface"]

                if (itf != null && itf != Object::class.java) {
                    replaceable.add(AnnotationNode(Opcodes.ASM9, ENVIRONMENT_INTERFACE_TYPE.descriptor).apply {
                        this.values = KiltMixinModifications.mapToAnnotationValues(mapOf(
                            "value" to DistUtil.distToEnvType(value),
                            "itf" to itf
                        ))
                    })
                }

                replaceable.add(AnnotationNode(Opcodes.ASM9, ENVIRONMENT_TYPE.descriptor).apply {
                    this.values = KiltMixinModifications.mapToAnnotationValues(mapOf(
                        "value" to DistUtil.distToEnvType(value)
                    ))
                })
            }
        }

        setter.accept(replaceable)
    }
}