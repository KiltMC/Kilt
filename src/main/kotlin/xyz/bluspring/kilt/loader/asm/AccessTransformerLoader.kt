package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.MappingResolver
import net.fabricmc.loader.impl.FabricLoaderImpl
import org.apache.commons.lang3.BitField
import org.objectweb.asm.Opcodes
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.regex.Pattern

// A reimplementation of Forge's Access Transformers.
// The specification can be found here: https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md
object AccessTransformerLoader {
    private val whitespace = Pattern.compile("[ \t]+")
    private val remapper = KiltRemapper.srgIntermediaryTree

    private val classTransformInfo = mutableMapOf<String, ClassTransformInfo>()

    fun convertTransformers(data: ByteArray) {
        val textData = String(data)
        val delimiter = if (textData.contains("\r\n")) "\r\n" else "\n"

        for (line in textData.split(delimiter)) {
            if (line.startsWith("#"))
                continue

            if (line.isBlank())
                continue

            val split = line.trim().split(whitespace)

            // access modifier
            val accessType = when (split[0].removeSuffix("-f").removeSuffix("+f")) {
                "public" -> AccessType.PUBLIC
                "protected" -> AccessType.PROTECTED
                "private" -> AccessType.PRIVATE
                "default" -> AccessType.PACKAGE_PRIVATE
                else -> throw IllegalArgumentException("Expected public/protected/private/default, got ${split[0]}")
            }

            val finalType = if (split[0].endsWith("-f"))
                Final.REMOVE
            else if (split[0].endsWith("+f"))
                Final.ADD
            else Final.DEFAULT

            // class name
            val srgClassName = split[1]
            val intermediaryClass = remapper.classes.firstOrNull { it.getName("srg") == srgClassName }
            val intermediaryClassName = intermediaryClass?.getName("intermediary") ?: srgClassName

            // field / method
            if (split.size > 2 && !split[2].startsWith("#")) {
                if (split[2].contains("(")) { // method
                    var name = ""
                    var descriptor = ""

                    run { // get descriptor
                        var isInDescriptor = false

                        for (char in split[2]) {
                            if (char == '(')
                                isInDescriptor = true

                            if (isInDescriptor)
                                descriptor += char
                            else
                                name += char
                        }
                    }

                    val methodData = intermediaryClass?.methods?.firstOrNull { it.getName("srg") == name && it.getDescriptor("srg") == name }
                    val transformInfo = classTransformInfo[intermediaryClassName] ?: ClassTransformInfo(AccessType.DEFAULT, Final.DEFAULT)
                    val pair = if (methodData == null) Pair(name, descriptor) else Pair(methodData.getName("intermediary"), methodData.getDescriptor("intermediary"))

                    if (transformInfo.methods.contains(pair)) {
                        val methodTransformInfo = transformInfo.methods[pair]!!

                        // promote access type
                        if (accessType.ordinal < methodTransformInfo.currentAccessType.ordinal) {
                            methodTransformInfo.currentAccessType = accessType
                        }

                        // promote final type
                        if (finalType.ordinal < methodTransformInfo.final.ordinal) {
                            methodTransformInfo.final = finalType
                        }
                    } else {
                        transformInfo.methods[pair] = TransformInfo(accessType, finalType)
                    }

                    if (!classTransformInfo.contains(intermediaryClassName))
                        classTransformInfo[intermediaryClassName] = transformInfo
                } else { // field
                    val name = split[2]

                    val fieldData = intermediaryClass?.fields?.firstOrNull { it.getName("srg") == name }
                    val fieldName = fieldData?.getName("intermediary") ?: name

                    val transformInfo = classTransformInfo[intermediaryClassName] ?: ClassTransformInfo(AccessType.DEFAULT, Final.DEFAULT)

                    if (transformInfo.fields.contains(fieldName)) {
                        val fieldTransformInfo = transformInfo.fields[fieldName]!!

                        // promote access type
                        if (accessType.ordinal < fieldTransformInfo.currentAccessType.ordinal) {
                            fieldTransformInfo.currentAccessType = accessType
                        }

                        // promote final type
                        if (finalType.ordinal < fieldTransformInfo.final.ordinal) {
                            fieldTransformInfo.final = finalType
                        }
                    } else {
                        transformInfo.fields[fieldName] = TransformInfo(accessType, finalType)
                    }

                    if (!classTransformInfo.contains(intermediaryClassName))
                        classTransformInfo[intermediaryClassName] = transformInfo
                }
            } else { // class
                val transformInfo = classTransformInfo[intermediaryClassName]

                if (transformInfo == null) {
                    classTransformInfo[intermediaryClassName] = ClassTransformInfo(accessType, finalType)
                } else {
                    // Promote the access type if the level is higher than the current one
                    if (accessType.ordinal < transformInfo.currentAccessType.ordinal) {
                        transformInfo.currentAccessType = accessType
                    }

                    // Also promote the final type
                    if (finalType.ordinal < transformInfo.final.ordinal) {
                        transformInfo.final = finalType
                    }
                }
            }
        }
    }

    fun runTransformers() {
        val remapper = FabricLoaderImpl.INSTANCE.mappingResolver

        classTransformInfo.forEach { (className, classTransformInfo) ->
            ClassTinkerers.addTransformation(className) { classNode ->
                val bitField = BitField(classNode.access)

                // access modifiers
                if (classTransformInfo.currentAccessType != AccessType.DEFAULT) {
                    AccessType.values().forEach { accessType ->
                        bitField.clear(accessType.flag)
                    }

                    bitField.set(classTransformInfo.currentAccessType.flag)
                }

                // final flag
                if (classTransformInfo.final != Final.DEFAULT) {
                    if (classTransformInfo.final == Final.ADD)
                        bitField.set(Opcodes.ACC_FINAL)
                    else
                        bitField.clear(Opcodes.ACC_FINAL)
                }

                val remappedClass = this.remapper.classes.firstOrNull { it.getName("intermediary") == className }

                classTransformInfo.fields.forEach field@{ (fieldName, fieldTransformInfo) ->
                    val intermediaryFieldDescriptor = remappedClass?.fields?.firstOrNull { it.getName("intermediary") == fieldName }?.getDescriptor("intermediary")
                    val mappedFieldName = if (intermediaryFieldDescriptor != null)
                        remapper.mapFieldName("intermediary", className, fieldName, intermediaryFieldDescriptor)
                    else fieldName

                    val fieldNode = classNode.fields.firstOrNull { it.name == mappedFieldName } ?: return@field
                    val fieldBitField = BitField(fieldNode.access)

                    // access modifiers
                    if (fieldTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            fieldBitField.clear(accessType.flag)
                        }

                        fieldBitField.set(fieldTransformInfo.currentAccessType.flag)
                    }

                    // final flag
                    if (fieldTransformInfo.final != Final.DEFAULT) {
                        if (fieldTransformInfo.final == Final.ADD)
                            fieldBitField.set(Opcodes.ACC_FINAL)
                        else
                            fieldBitField.clear(Opcodes.ACC_FINAL)
                    }
                }

                classTransformInfo.methods.forEach method@{ (pair, methodTransformInfo) ->
                    val name = pair.first
                    val descriptor = pair.second

                    val mappedMethodName = remapper.mapMethodName("intermediary", className, name, descriptor)
                    val mappedDescriptor = remapDescriptor(descriptor, "intermediary", remapper)

                    val methodNode = classNode.methods.firstOrNull { it.name == mappedMethodName && it.desc == mappedDescriptor } ?: return@method
                    val methodBitField = BitField(methodNode.access)

                    // access modifiers
                    if (methodTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            methodBitField.clear(accessType.flag)
                        }

                        methodBitField.set(methodTransformInfo.currentAccessType.flag)
                    }

                    // final flag
                    if (methodTransformInfo.final != Final.DEFAULT) {
                        if (methodTransformInfo.final == Final.ADD)
                            methodBitField.set(Opcodes.ACC_FINAL)
                        else
                            methodBitField.clear(Opcodes.ACC_FINAL)
                    }
                }
            }
        }
    }

    private fun remapDescriptor(descriptor: String, namespace: String, remapper: MappingResolver): String {
        var newDescriptor = ""

        var isInClass = false
        var className = ""
        descriptor.forEach {
            if (it == ';' && isInClass) {
                isInClass = false

                newDescriptor += remapper.mapClassName(namespace, className)
                className = ""
                newDescriptor += ';'

                return@forEach
            }

            if (!isInClass)
                newDescriptor += it
            else
                className += it

            if (it == 'L' && !isInClass)
                isInClass = true
        }

        return newDescriptor
    }

    private enum class AccessType(val flag: Int) {
        PUBLIC(Opcodes.ACC_PUBLIC), PROTECTED(Opcodes.ACC_PROTECTED), PACKAGE_PRIVATE(0), PRIVATE(Opcodes.ACC_PRIVATE), DEFAULT(0)
    }

    private enum class Final {
        REMOVE, ADD, DEFAULT
    }

    private data class ClassTransformInfo(
        var currentAccessType: AccessType,
        var final: Final,
        val fields: MutableMap<String, TransformInfo> = mutableMapOf(),
        val methods: MutableMap<Pair<String, String>, TransformInfo> = mutableMapOf()
    )

    private data class TransformInfo(
        var currentAccessType: AccessType,
        var final: Final
    )
}