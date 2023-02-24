package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.MappingResolver
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.mapping.tree.TinyTree
import org.apache.commons.lang3.BitField
import org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.regex.Pattern

// A reimplementation of Forge's Access Transformers.
// The specification can be found here: https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md
object AccessTransformerLoader {
    private val logger = LoggerFactory.getLogger("Kilt Access Transformers")

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
            val intermediaryClass = remapper.classes.firstOrNull { it.getName("srg") == srgClassName.replace(".", "/") }
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

                    val intermediaryDescriptor = remapDescriptor(descriptor, "srg", "intermediary", remapper)

                    val methodData = intermediaryClass?.methods?.firstOrNull {
                        it.getName("srg") == name
                                && it.getDescriptor("intermediary") == intermediaryDescriptor
                    }
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
            val mappedClassName = remapper.mapClassName("intermediary", className.replace("/", "."))

            ClassTinkerers.addTransformation(mappedClassName) { classNode ->
                println("access transforming class $mappedClassName")

                val bitField = BitField(classNode.access)

                // access modifiers
                if (classTransformInfo.currentAccessType != AccessType.DEFAULT) {
                    AccessType.values().forEach { accessType ->
                        bitField.clear(accessType.flag)
                    }

                    bitField.set(classTransformInfo.currentAccessType.flag)
                    println("set class to access type ${classTransformInfo.currentAccessType.name}")
                }

                // final flag
                if (classTransformInfo.final != Final.DEFAULT) {
                    if (classTransformInfo.final == Final.ADD)
                        bitField.set(Opcodes.ACC_FINAL)
                    else
                        bitField.clear(Opcodes.ACC_FINAL)

                    println("set class to final type ${classTransformInfo.final.name}")
                }

                val remappedClass = this.remapper.classes.firstOrNull { it.getName("intermediary") == className }

                classTransformInfo.fields.forEach field@{ (fieldName, fieldTransformInfo) ->
                    val intermediaryFieldDescriptor = remappedClass?.fields?.firstOrNull { it.getName("intermediary") == fieldName }?.getDescriptor("intermediary")
                    val mappedFieldName = if (intermediaryFieldDescriptor != null)
                        remapper.mapFieldName("intermediary", className.replace("/", "."), fieldName, intermediaryFieldDescriptor)
                    else fieldName

                    println("transforming field $mappedFieldName")

                    val fieldNode = classNode.fields.firstOrNull { it.name == mappedFieldName } ?: return@field
                    val fieldBitField = BitField(fieldNode.access)

                    // access modifiers
                    if (fieldTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            fieldBitField.clear(accessType.flag)
                        }

                        fieldBitField.set(fieldTransformInfo.currentAccessType.flag)

                        println("set field to access type ${fieldTransformInfo.currentAccessType.name}")
                    }

                    // final flag
                    if (fieldTransformInfo.final != Final.DEFAULT) {
                        if (fieldTransformInfo.final == Final.ADD)
                            fieldBitField.set(Opcodes.ACC_FINAL)
                        else
                            fieldBitField.clear(Opcodes.ACC_FINAL)

                        println("set field to final type ${fieldTransformInfo.final.name}")
                    }
                }

                classTransformInfo.methods.forEach method@{ (pair, methodTransformInfo) ->
                    val name = pair.first
                    val descriptor = pair.second

                    val mappedMethodName = remapper.mapMethodName("intermediary", className.replace("/", "."), name, descriptor)
                    val mappedDescriptor = remapDescriptor(descriptor, "intermediary", remapper)

                    println("transforming method $mappedMethodName$mappedDescriptor")

                    val methodNode = classNode.methods.firstOrNull { it.name == mappedMethodName && it.desc == mappedDescriptor } ?: return@method
                    val methodBitField = BitField(methodNode.access)

                    // access modifiers
                    if (methodTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            methodBitField.clear(accessType.flag)
                        }

                        methodBitField.set(methodTransformInfo.currentAccessType.flag)

                        println("set method to access type ${methodTransformInfo.currentAccessType.name}")
                    }

                    // final flag
                    if (methodTransformInfo.final != Final.DEFAULT) {
                        if (methodTransformInfo.final == Final.ADD)
                            methodBitField.set(Opcodes.ACC_FINAL)
                        else
                            methodBitField.clear(Opcodes.ACC_FINAL)

                        println("set method to final type ${methodTransformInfo.final.name}")
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

                newDescriptor += remapper.mapClassName(namespace, className.replace("/", ".")).replace(".", "/")
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

    private fun remapDescriptor(descriptor: String, from: String, to: String, tree: TinyTree): String {
        var newDescriptor = ""

        var isInClass = false
        var className = ""
        descriptor.forEach {
            if (it == ';' && isInClass) {
                isInClass = false

                val classDef = tree.classes.firstOrNull { def -> def.getName(from) == className }
                newDescriptor += classDef?.getName(to) ?: className
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