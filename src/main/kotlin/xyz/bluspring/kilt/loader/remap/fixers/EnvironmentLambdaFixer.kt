package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.api.Environment
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.invoke.LambdaMetafactory

object EnvironmentLambdaFixer {
    val LAMBDA_CLASS_NAME = Type.getInternalName(LambdaMetafactory::class.java)!!
    const val LAMBDA_METHOD_DESCRIPTOR = "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
    private val ENVIRONMENT_TYPE = Type.getType(Environment::class.java)

    fun fixClass(classNode: ClassNode) {
        val methodsToMark = mutableMapOf<Pair<String, String>, AnnotationNode>()

        for (methodNode in classNode.methods) {
            val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
            if (annotations.none { it.desc == ENVIRONMENT_TYPE.descriptor })
                continue

            val envAnnotation = annotations.first { it.desc == ENVIRONMENT_TYPE.descriptor }

            for (insnNode in methodNode.instructions) {
                // Modified from Quilt - https://github.com/QuiltMC/quilt-loader/blob/develop/src/main/java/org/quiltmc/loader/impl/transformer/LambdaStripCalculator.java
                if (insnNode is InvokeDynamicInsnNode) {
                    if (Opcodes.H_INVOKESTATIC != insnNode.bsm.tag)
                        continue

                    if ("metafactory" != insnNode.bsm.name)
                        continue

                    if (LAMBDA_CLASS_NAME != insnNode.bsm.owner)
                        continue

                    if (LAMBDA_METHOD_DESCRIPTOR != insnNode.bsm.desc)
                        continue

                    if (insnNode.bsmArgs?.size == 3) {
                        if (insnNode.bsmArgs[1] is Handle) {
                            val lambdaTarget = insnNode.bsmArgs[1] as Handle
                            if (lambdaTarget.owner == classNode.name) {
                                methodsToMark[Pair(lambdaTarget.name, lambdaTarget.desc)] = envAnnotation
                            }
                        }
                    }
                }
            }
        }

        for ((pair, annotationNode) in methodsToMark) {
            val (methodName, methodDesc) = pair
            val methodNode = classNode.methods.firstOrNull { it.name == methodName && it.desc == methodDesc } ?: continue

            if (methodNode.visibleAnnotations != null) {
                val list = mutableListOf<AnnotationNode>()
                list.addAll(methodNode.visibleAnnotations)
                list.add(annotationNode)
                methodNode.visibleAnnotations = list
            } else {
                val list = mutableListOf<AnnotationNode>()
                list.add(annotationNode)
                methodNode.visibleAnnotations = list
            }
        }
    }
}