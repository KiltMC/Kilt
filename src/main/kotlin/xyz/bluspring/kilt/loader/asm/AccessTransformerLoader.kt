package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.regex.Pattern

// A reimplementation of Forge's Access Transformers.
// The specification can be found here: https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md
object AccessTransformerLoader {
    private val logger = LoggerFactory.getLogger("Kilt Access Transformers")
    private const val debug = false
    private var hasLoaded = false

    private val whitespace = Pattern.compile("[ \t]+")

    private val classTransformInfo = mutableMapOf<String, ClassTransformInfo>()
    private val mc = KiltRemapper.mcRemapper
    private val mappingResolver = FabricLoader.getInstance().mappingResolver

    private fun println(info: String) {
        if (debug)
            logger.info(info)
    }

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
            val intermediaryClassName = KiltRemapper.remapClass(srgClassName.replace(".", "/"))

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

                    val intermediaryDescriptor = KiltRemapper.remapDescriptor(descriptor, toIntermediary = true)
                    val mappedDescriptor = KiltRemapper.remapDescriptor(descriptor)

                    val methodName = mappingResolver.mapMethodName("intermediary", intermediaryClassName.replace("/", "."), mc.mapFieldName(srgClassName, name, descriptor), intermediaryDescriptor)
                    val transformInfo = classTransformInfo[intermediaryClassName] ?: ClassTransformInfo(AccessType.DEFAULT, Final.DEFAULT)
                    val pair = Pair(methodName, mappedDescriptor)

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

                    val fieldInfo = KiltRemapper.srgIntermediaryTree.classes.firstOrNull { it.getName("searge") == srgClassName }?.fields?.firstOrNull { it.getName("searge") == name } ?: continue
                    val fieldName = mappingResolver.mapMethodName("intermediary", intermediaryClassName.replace("/", "."), mc.mapFieldName(srgClassName, name, fieldInfo.getDescriptor("searge")), fieldInfo.getDescriptor("intermediary"))

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
        if (hasLoaded)
            return

        val startTime = System.currentTimeMillis()
        logger.info("Adding access transformers to mixin")

        classTransformInfo.forEach { (mappedClassName, classTransformInfo) ->
            ClassTinkerers.addTransformation(mappedClassName) { classNode ->
                println("access transforming class $mappedClassName")

                // access modifiers
                if (classTransformInfo.currentAccessType != AccessType.DEFAULT) {
                    AccessType.values().forEach { accessType ->
                        // clear bits
                        classNode.access = classNode.access and accessType.flag.inv()
                    }

                    classNode.access = classNode.access or classTransformInfo.currentAccessType.flag
                    println("set class to access type ${classTransformInfo.currentAccessType.name}")
                }

                // final flag
                if (classTransformInfo.final != Final.DEFAULT) {
                    classNode.access = if (classTransformInfo.final == Final.ADD)
                        classNode.access or Opcodes.ACC_FINAL // set bits
                    else
                        classNode.access and Opcodes.ACC_FINAL.inv() // clear bits

                    println("set class to final type ${classTransformInfo.final.name}")
                }

                classTransformInfo.fields.forEach field@{ (fieldName, fieldTransformInfo) ->
                    val mappedFieldName = fieldName

                    println("transforming field $mappedFieldName")

                    val fieldNode = classNode.fields.firstOrNull { it.name == mappedFieldName } ?: return@field

                    // access modifiers
                    if (fieldTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            // clear bits
                            fieldNode.access = fieldNode.access and accessType.flag.inv()
                        }

                        // add bits
                        fieldNode.access = fieldNode.access or fieldTransformInfo.currentAccessType.flag

                        println("set field to access type ${fieldTransformInfo.currentAccessType.name}")
                    }

                    // final flag
                    if (fieldTransformInfo.final != Final.DEFAULT) {
                        if (fieldTransformInfo.final == Final.ADD)
                            // add bits
                            fieldNode.access = fieldNode.access or Opcodes.ACC_FINAL
                        else
                            // clear bits
                            fieldNode.access = fieldNode.access and Opcodes.ACC_FINAL.inv()

                        println("set field to final type ${fieldTransformInfo.final.name}")
                    }
                }

                classTransformInfo.methods.forEach method@{ (pair, methodTransformInfo) ->
                    val mappedMethodName = pair.first
                    val mappedDescriptor = pair.second

                    println("transforming method $mappedMethodName$mappedDescriptor")

                    val methodNode = classNode.methods.firstOrNull { it.name == mappedMethodName && it.desc == mappedDescriptor } ?: return@method

                    // access modifiers
                    if (methodTransformInfo.currentAccessType != AccessType.DEFAULT) {
                        AccessType.values().forEach { accessType ->
                            // clear bits
                            methodNode.access = methodNode.access and accessType.flag.inv()
                        }

                        // add bits
                        methodNode.access = methodNode.access or methodTransformInfo.currentAccessType.flag

                        println("set method to access type ${methodTransformInfo.currentAccessType.name}")
                    }

                    // final flag
                    if (methodTransformInfo.final != Final.DEFAULT) {
                        if (methodTransformInfo.final == Final.ADD)
                        // add bits
                            methodNode.access = methodNode.access or Opcodes.ACC_FINAL
                        else
                        // clear bits
                            methodNode.access = methodNode.access and Opcodes.ACC_FINAL.inv()

                        println("set method to final type ${methodTransformInfo.final.name}")
                    }
                }
            }
        }

        logger.info("Finished loading access transformers (took ${System.currentTimeMillis() - startTime}ms)")
        hasLoaded = true
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