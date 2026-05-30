package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import com.llamalad7.mixinextras.sugar.Local
import com.llamalad7.mixinextras.sugar.ref.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.LocalPair
import kotlin.math.max

// Used for retargeting any local targets that were messed up by Neo's recompiling.
data class RetargetingLocalModifier(
    override val owner: String,
    override val methods: List<String>,
    val paramToLocalMapping: Map<LocalPair, Local>
) : MethodBasedModifier {
    companion object {
        val LOCAL = Type.getType(Local::class.java)
    }

    override lateinit var mappedOwner: String
    override lateinit var mappedMethods: List<String>

    private fun <T> matches(value: T?, target: T, default: T): Boolean {
        if (value == null)
            return target == default

        if (value is Collection<*> && target is Collection<*>) {
            if (value.isEmpty() && target.isEmpty())
                return true

            return value.containsAll(target)
        }

        if (value is Array<*> && target is Array<*>) {
            if (value.isEmpty() && target.isEmpty())
                return true

            return value.contentEquals(target)
        }

        return value == target
    }

    private val boolRefType = Type.getType(LocalBooleanRef::class.java)
    private val charRefType = Type.getType(LocalCharRef::class.java)
    private val byteRefType = Type.getType(LocalByteRef::class.java)
    private val shortRefType = Type.getType( LocalShortRef::class.java)
    private val intRefType = Type.getType(LocalIntRef::class.java)
    private val longRefType = Type.getType(LocalLongRef::class.java)
    private val doubleRefType = Type.getType(LocalDoubleRef::class.java)
    private val floatRefType = Type.getType(LocalFloatRef::class.java)
    private val objectRefType = Type.getType(LocalRef::class.java)

    fun unboxRefDescriptor(localDescriptor: Type, localSignature: String?): Type {
        return when (localDescriptor.internalName) {
            boolRefType.internalName -> Type.BOOLEAN_TYPE
            charRefType.internalName -> Type.CHAR_TYPE
            byteRefType.internalName -> Type.BYTE_TYPE
            shortRefType.internalName -> Type.SHORT_TYPE
            intRefType.internalName -> Type.INT_TYPE
            longRefType.internalName -> Type.LONG_TYPE
            doubleRefType.internalName -> Type.DOUBLE_TYPE
            floatRefType.internalName -> Type.FLOAT_TYPE
            objectRefType.internalName -> {
                if (localSignature?.startsWith("L${objectRefType.internalName}<") == true && localSignature.endsWith(">;")) {
                    val unboxedSignature = localSignature.substring(objectRefType.descriptor.length, localSignature.length-1)
                    if (unboxedSignature.contains("<")) {
                        Type.getType("${unboxedSignature.substring(0, unboxedSignature.indexOf("<"))};")
                    } else {
                        Type.getType(unboxedSignature)
                    }
                } else {
                    localDescriptor
                }
            }
            else -> localDescriptor
        }
    }

    // Copied from Type::getArgumentTypes
    // Modified to handle generic types.
    private fun splitSignature(argumentCount: Int, methodSignature: String): Array<String> {
        val argumentTypes = Array(argumentCount) { "" }
        var currentOffset = 1
        var currentArgumentTypeIndex = 0

        while (methodSignature.get(currentOffset) != ')') {
            val currentArgumentTypeOffset = currentOffset
            while (methodSignature.get(currentOffset) == '[') {
                currentOffset++
            }
            if (methodSignature.get(currentOffset++) == 'L') {
                // Skip the argument descriptor content.
                var semiColumnOffset = methodSignature.indexOf(';', currentOffset)
                val genericIndex = methodSignature.indexOf("<", currentOffset)
                if (genericIndex != -1 && genericIndex < semiColumnOffset) {
                    var depth = 1
                    currentOffset = genericIndex+1
                    while (depth > 0) {
                        when (methodSignature.get(currentOffset)) {
                            '<' -> depth++
                            '>' -> depth--
                        }
                        currentOffset++
                    }
                    semiColumnOffset = methodSignature.indexOf(';', currentOffset)
                }
                currentOffset = max(currentOffset, semiColumnOffset + 1)
            }
            argumentTypes[currentArgumentTypeIndex++] = methodSignature.substring(currentArgumentTypeOffset, currentOffset)
        }
        return argumentTypes
    }

    fun retargetLocals(methodNode: MethodNode) {
        val paramAnnotations = methodNode.invisibleParameterAnnotations ?: return
        val splitDescriptor = Type.getArgumentTypes(methodNode.desc)
        val splitSignature = if (methodNode.signature != null) splitSignature(splitDescriptor.size, methodNode.signature) else null
        val modifiedParamAnnotations = paramAnnotations.toMutableList()

        var hasModified = false
        for ((i, annotations) in paramAnnotations.withIndex()) {
            if (annotations == null)
                continue

            val descriptor = splitDescriptor[i]
            val signature = splitSignature?.get(i)
            val newAnnotations = annotations.toMutableList()

            for ((j, annotation) in annotations.withIndex()) {
                if (annotation.desc == LOCAL.descriptor) {
                    val values = KiltMixinModifications.annotationValuesToMap(annotation.values ?: listOf())
                    val ordinal = values["ordinal"]
                    val print = values["print"]
                    val index = values["index"]
                    val name = values["name"]
                    val argsOnly = values["argsOnly"]
                    val type = values["type"]

                    val mapping = paramToLocalMapping.filter {
                            it.key.descriptor == (if (it.key.unboxRef) unboxRefDescriptor(descriptor, signature).descriptor else descriptor.descriptor)
                            && (matches(ordinal, it.key.local.ordinal, -1)
                            && matches(print, it.key.local.print, false)
                            && matches(index, it.key.local.index, -1)
                            && matches(name, it.key.local.name.toList(), listOf<String>())
                            && matches(argsOnly, it.key.local.argsOnly, false)
                            && matches(type, it.key.local.type, Unit::class)
                        )
                    }
                    val remap = mapping.values.firstOrNull() ?: continue

                    newAnnotations[j] = AnnotationNode(Opcodes.ASM9, annotation.desc).apply {
                        this.values = KiltMixinModifications.mapToAnnotationValues(
                            mapOf(
                                "ordinal" to remap.ordinal,
                            )
                        )
                    }

                    hasModified = true
                }
            }

            modifiedParamAnnotations[i] = newAnnotations
        }

        if (hasModified) {
            methodNode.invisibleParameterAnnotations = modifiedParamAnnotations.toTypedArray()
        }
    }
}
