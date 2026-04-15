package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.api.Environment
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.invoke.LambdaMetafactory

object EnvironmentLambdaFixer {
    val LAMBDA_CLASS_NAME = Type.getInternalName(LambdaMetafactory::class.java)
    const val LAMBDA_METHOD_DESCRIPTOR = "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
    private val ENVIRONMENT_TYPE = Type.getType(Environment::class.java)

    private fun getAnnotations(methodNode: MethodNode): Collection<AnnotationNode> {
        return KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
    }

    private fun lacksEnvAnnotation(annotations: Collection<AnnotationNode>): Boolean {
        return annotations.none { it.desc == ENVIRONMENT_TYPE.descriptor }
    }

    private fun lacksEnvAnnotation(methodNode: MethodNode): Boolean {
        return lacksEnvAnnotation(getAnnotations(methodNode))
    }

    private fun markLambdas(
        methodNode: MethodNode, classNode: ClassNode, envAnnotation: AnnotationNode,
        methodsToMark: MutableMap<Pair<String, String>, AnnotationNode>
    ) {
        val nestedLambdas = mutableSetOf<String>()
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
                            methodsToMark[lambdaTarget.name to lambdaTarget.desc] = envAnnotation
                            nestedLambdas.add(lambdaTarget.name)
                        }
                    }
                }
            }
        }
        for (nestedNode in classNode.methods) {
            if (nestedLambdas.contains(nestedNode.name) && lacksEnvAnnotation(nestedNode)) {
                markLambdas(nestedNode, classNode, envAnnotation, methodsToMark)
            }
        }
    }

    fun fixClass(classNode: ClassNode) {
        val methodsToMark = mutableMapOf<Pair<String, String>, AnnotationNode>()

        for (methodNode in classNode.methods) {
            val annotations = getAnnotations(methodNode)
            if (lacksEnvAnnotation(annotations))
                continue

            val envAnnotation = annotations.firstOrNull { it.desc == ENVIRONMENT_TYPE.descriptor } ?: continue

            markLambdas(methodNode, classNode, envAnnotation, methodsToMark)
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