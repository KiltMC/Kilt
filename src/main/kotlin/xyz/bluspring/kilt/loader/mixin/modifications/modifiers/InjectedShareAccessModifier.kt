package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import com.llamalad7.mixinextras.sugar.Local
import com.llamalad7.mixinextras.sugar.Share
import com.llamalad7.mixinextras.sugar.ref.*
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifier
import xyz.bluspring.kilt.loader.mixin.modifications.ParamPair
import java.lang.reflect.Modifier

data class InjectedShareAccessModifier(
    override val owner: String,
    val methods: List<String>,
    val paramToShareMapping: Map<ParamPair, Share>
) : MixinModifier {
    override lateinit var mappedOwner: String

    fun injectShareAccess(mixinClassNode: ClassNode, methodNode: MethodNode, annotation: AnnotationNode, copiedMethodName: String): MethodNode {
        // Now try to create our custom share-based injection.
        val newMethod = MethodNode()
        newMethod.name = methodNode.name
        newMethod.access = methodNode.access
        newMethod.signature = methodNode.signature

        newMethod.visibleAnnotations = methodNode.visibleAnnotations
        newMethod.invisibleAnnotations = methodNode.invisibleAnnotations
        newMethod.visibleTypeAnnotations = methodNode.visibleTypeAnnotations
        newMethod.invisibleTypeAnnotations = methodNode.invisibleTypeAnnotations
        newMethod.visibleParameterAnnotations = methodNode.visibleParameterAnnotations
        newMethod.invisibleAnnotableParameterCount = methodNode.invisibleAnnotableParameterCount
        newMethod.visibleAnnotableParameterCount = methodNode.visibleAnnotableParameterCount
        newMethod.attrs = methodNode.attrs
        newMethod.exceptions = methodNode.exceptions

        val returnType = Type.getReturnType(methodNode.desc)
        val splitDescriptor = Type.getArgumentTypes(methodNode.desc)
        val modifiedSplitDescriptor = splitDescriptor.toMutableList()
        val paramAnnotations = newMethod.invisibleParameterAnnotations ?: Array(splitDescriptor.size) { mutableListOf() }
        val shareIndices = mutableListOf<Int>()

        val splitSignature = KiltMixinModifier.splitSignature((methodNode.signature ?: methodNode.desc).removePrefix("(").replaceAfter(")", "").removeSuffix(")"))
        val modifiedSplitSignature = splitSignature.toMutableList()

        for ((paramPair, share) in this.paramToShareMapping) {
            val (desc, ordinal) = paramPair
            var totalFoundMatchingDesc = 0
            var foundIndex = -1

            for ((index, descPart) in splitDescriptor.withIndex()) {
                if (descPart.descriptor == desc && totalFoundMatchingDesc++ == ordinal) {
                    foundIndex = index
                    break
                }
            }

            if (foundIndex == -1)
                throw IllegalStateException("Failed to locate param pair $paramPair for ${methodNode.name} in ${mixinClassNode.name}!")

            modifiedSplitDescriptor[foundIndex] = when (desc) {
                "I" -> LOCAL_INT_REF
                "Z" -> LOCAL_BOOLEAN_REF
                "B" -> LOCAL_BYTE_REF
                "C" -> LOCAL_CHAR_REF
                "S" -> LOCAL_SHORT_REF
                "J" -> LOCAL_LONG_REF
                "F" -> LOCAL_FLOAT_REF
                "D" -> LOCAL_DOUBLE_REF

                else -> LOCAL_REF
            }

            modifiedSplitSignature[foundIndex] = when (desc) {
                "I" -> LOCAL_INT_REF.descriptor
                "Z" -> LOCAL_BOOLEAN_REF.descriptor
                "B" -> LOCAL_BYTE_REF.descriptor
                "C" -> LOCAL_CHAR_REF.descriptor
                "S" -> LOCAL_SHORT_REF.descriptor
                "J" -> LOCAL_LONG_REF.descriptor
                "F" -> LOCAL_FLOAT_REF.descriptor
                "D" -> LOCAL_DOUBLE_REF.descriptor

                else -> "${LOCAL_REF.descriptor.removeSuffix(";")}<${desc}>;"
            }

            shareIndices.add(foundIndex)

            paramAnnotations[foundIndex].add(KiltMixinModifications.createAnnotation(Share::class.java, mutableMapOf<String, Any>(
                "value" to share.value,
                "namespace" to share.namespace
            )))
        }

        // Local capture to sugar annotation handling :D
        if (KiltMixinModifications.annotationValuesToMap(annotation.values).contains("locals")) {
            val callbackInfoIndex = splitDescriptor.indexOfLast { it == CALLBACK_INFO || it == CALLBACK_INFO_RETURNABLE }

            if (callbackInfoIndex == -1)
                throw IllegalStateException("How are we at negative index here?")

            val totalOccurrences = mutableMapOf<String, Int>()

            for ((index, descPart) in splitDescriptor.withIndex()) {
                // Skip our shares.
                if (shareIndices.contains(index))
                    continue

                if (index > callbackInfoIndex) {
                    paramAnnotations[index + callbackInfoIndex].add(KiltMixinModifications.createAnnotation(Local::class.java, mapOf(
                        "ordinal" to totalOccurrences.getOrDefault(descPart.descriptor, 0)
                    )))
                }

                totalOccurrences[descPart.descriptor] = totalOccurrences.getOrDefault(descPart.descriptor, 0) + 1
            }
        }

        newMethod.invisibleParameterAnnotations = paramAnnotations

        newMethod.desc = "(${modifiedSplitDescriptor.joinToString("")})${returnType}"
        newMethod.signature = "(${modifiedSplitSignature.joinToString("")})${returnType}"

        newMethod.visitCode()

        val label0 = Label()
        val label1 = Label()
        val label2 = Label()

        newMethod.visitLabel(label0)

        if (!Modifier.isStatic(newMethod.access))
            newMethod.visitVarInsn(Opcodes.ALOAD, 0)

        val indexOffset = if (Modifier.isStatic(newMethod.access)) 0 else 1

        for ((index, descPart) in splitDescriptor.withIndex()) {
            if (!shareIndices.contains(index)) {
                // We can do a regular variable load if this isn't a share.
                newMethod.visitVarInsn(
                    when (descPart.descriptor) {
                        "I", "Z", "S", "B", "C" -> Opcodes.ILOAD
                        "J" -> Opcodes.LLOAD
                        "D" -> Opcodes.DLOAD
                        "F" -> Opcodes.FLOAD

                        else -> Opcodes.ALOAD
                    }, index + indexOffset
                )
            } else {
                // Otherwise, time to call the getter.
                newMethod.visitVarInsn(Opcodes.ALOAD, index + indexOffset)
                newMethod.visitMethodInsn(Opcodes.INVOKEINTERFACE, modifiedSplitDescriptor[index].descriptor.removePrefix("L").removeSuffix(";"), "get", "()Ljava/lang/Object;", true)
                newMethod.visitTypeInsn(Opcodes.CHECKCAST, descPart.descriptor.removeSurrounding("L", ";"))
            }
        }

        newMethod.visitMethodInsn(
            if (indexOffset == 1) Opcodes.INVOKEVIRTUAL else Opcodes.INVOKESTATIC,
            mixinClassNode.name,
            copiedMethodName, methodNode.desc,
            false
        )

        newMethod.visitLabel(label1)
        newMethod.visitInsn(
            when (returnType.descriptor) {
                "V" -> Opcodes.RETURN
                "I", "Z", "S", "B", "C" -> Opcodes.IRETURN
                "J" -> Opcodes.LRETURN
                "D" -> Opcodes.DRETURN
                "F" -> Opcodes.FRETURN
                else -> Opcodes.ARETURN
            }
        )

        newMethod.visitLabel(label2)

        for ((index, descPart) in splitDescriptor.withIndex()) {
            if (index == 0 && indexOffset == 1) {
                newMethod.visitLocalVariable("this", Type.getObjectType(mixinClassNode.name).descriptor, null, label0, label2, index)
            }

            if (shareIndices.contains(index)) {
                val localRefDesc = modifiedSplitDescriptor[index]
                newMethod.visitLocalVariable("var${index}", localRefDesc.descriptor, modifiedSplitSignature[index], label0, label2, index + indexOffset)
            } else {
                newMethod.visitLocalVariable("var${index}", descPart.descriptor, null, label0, label2, index + indexOffset)
            }
        }

        newMethod.visitEnd()
        newMethod.visitMaxs(1, splitDescriptor.size + indexOffset)

        return newMethod
    }

    companion object {
        val CALLBACK_INFO = Type.getType(CallbackInfo::class.java)
        val CALLBACK_INFO_RETURNABLE = Type.getType(CallbackInfoReturnable::class.java)

        val LOCAL_REF = Type.getType(LocalRef::class.java)
        val LOCAL_INT_REF = Type.getType(LocalIntRef::class.java)
        val LOCAL_BYTE_REF = Type.getType(LocalByteRef::class.java)
        val LOCAL_FLOAT_REF = Type.getType(LocalFloatRef::class.java)
        val LOCAL_DOUBLE_REF = Type.getType(LocalDoubleRef::class.java)
        val LOCAL_SHORT_REF = Type.getType(LocalShortRef::class.java)
        val LOCAL_LONG_REF = Type.getType(LocalLongRef::class.java)
        val LOCAL_CHAR_REF = Type.getType(LocalCharRef::class.java)
        val LOCAL_BOOLEAN_REF = Type.getType(LocalBooleanRef::class.java)
    }
}