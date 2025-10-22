package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.lib.accesswidener.AccessWidener
import net.fabricmc.loader.impl.lib.accesswidener.AccessWidenerReader
import org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.regex.Pattern

// A reimplementation of Forge's Access Transformers.
// The specification can be found here: https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md
object AccessTransformerLoader {
    private val logger = LoggerFactory.getLogger("Kilt Access Transformers")
    private val debug = KiltFlags.ENABLE_ACCESS_TRANSFORMER_DEBUG
    private var hasLoaded = false

    private val classTransformInfo = mutableMapOf<String, ClassTransformInfo>()

    private val whitespace = Pattern.compile("[ \t]+")

    private fun println(info: String) {
        if (debug)
            logger.info(info)
        else
            logger.debug(info)
    }

    fun convertTransformers(data: ByteArray) {
        val accessWidener = FabricLoaderImpl.INSTANCE.accessWidener

        val textData = String(data)
        val delimiter = if (textData.contains("\r\n")) "\r\n" else "\n"

        for ((index, line) in textData.split(delimiter).withIndex()) {
            if (line.startsWith("#"))
                continue

            if (line.isBlank())
                continue

            try {
                val split = line.trim().split(whitespace)

                // class name
                val srgClassName = split[1].replace(".", "/")

                if (((srgClassName.startsWith("net/minecraft/") || srgClassName.startsWith("com/mojang/"))
                            // Handle access widening over Fabric
                            && !widenAccessForVanillaClasses(split, srgClassName, accessWidener))
                ) {
                    // Otherwise, this is for every other class
                    handleAccessTransform(split, srgClassName)
                }
            } catch (e: Throwable) {
                throw RuntimeException("Failed to process access transformer at line ${index + 1}: \"$line\"", e)
            }
        }
    }

    private fun handleAccessTransform(split: List<String>, className: String) {
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

                val methodName = name
                val transformInfo = classTransformInfo[className] ?: ClassTransformInfo(AccessType.DEFAULT, Final.DEFAULT)
                val pair = Pair(methodName, descriptor)

                val priorityAccess = accessType
                val priorityFinal = finalType

                if (transformInfo.methods.contains(pair)) {
                    val methodTransformInfo = transformInfo.methods[pair]!!

                    // promote access type
                    if (priorityAccess.ordinal < methodTransformInfo.currentAccessType.ordinal) {
                        methodTransformInfo.currentAccessType = priorityAccess
                    }

                    // promote final type
                    if (priorityFinal.ordinal < methodTransformInfo.final.ordinal) {
                        methodTransformInfo.final = priorityFinal
                    }
                } else {
                    transformInfo.methods[pair] = TransformInfo(priorityAccess, priorityFinal)
                }

                if (!classTransformInfo.contains(className))
                    classTransformInfo[className] = transformInfo
            } else { // field
                val name = split[2]

                val fieldName = name

                val transformInfo = classTransformInfo[className] ?: ClassTransformInfo(AccessType.DEFAULT, Final.DEFAULT)

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

                if (!classTransformInfo.contains(className))
                    classTransformInfo[className] = transformInfo
            }
        } else { // class
            val transformInfo = classTransformInfo[className]

            val priorityAccess = accessType

            val priorityFinal = finalType

            if (transformInfo == null) {
                classTransformInfo[className] = ClassTransformInfo(priorityAccess, priorityFinal)
            } else {
                // Promote the access type if the level is higher than the current one
                if (priorityAccess.ordinal < transformInfo.currentAccessType.ordinal) {
                    transformInfo.currentAccessType = priorityAccess
                }

                // Also promote the final type
                if (priorityFinal.ordinal < transformInfo.final.ordinal) {
                    transformInfo.final = priorityFinal
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

                    classNode.fields.filter { it.name == mappedFieldName || mappedFieldName == "*" }.forEach { fieldNode ->
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

                    if (mappedFieldName == "*") {
                        classNode.methods.forEach { methodNode ->
                            // access modifiers
                            if (fieldTransformInfo.currentAccessType != AccessType.DEFAULT) {
                                AccessType.values().forEach { accessType ->
                                    // clear bits
                                    methodNode.access = methodNode.access and accessType.flag.inv()
                                }

                                // add bits
                                methodNode.access = methodNode.access or fieldTransformInfo.currentAccessType.flag

                                println("set method to access type ${fieldTransformInfo.currentAccessType.name}")
                            }

                            // final flag
                            if (fieldTransformInfo.final != Final.DEFAULT) {
                                if (fieldTransformInfo.final == Final.ADD)
                                // add bits
                                    methodNode.access = methodNode.access or Opcodes.ACC_FINAL
                                else
                                // clear bits
                                    methodNode.access = methodNode.access and Opcodes.ACC_FINAL.inv()

                                println("set method to final type ${fieldTransformInfo.final.name}")
                            }
                        }
                    }
                }

                classTransformInfo.methods.forEach method@{ (pair, methodTransformInfo) ->
                    val mappedMethodName = pair.first
                    val mappedDescriptor = pair.second

                    println("transforming method $mappedMethodName$mappedDescriptor")

                    classNode.methods.filter { (it.name == mappedMethodName || it.name == "*") && (it.desc == "()" || it.desc == mappedDescriptor) }.forEach { methodNode ->
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
        }

        logger.info("Finished loading access transformers (took ${System.currentTimeMillis() - startTime}ms)")
        hasLoaded = true
    }

    private fun widenAccessForVanillaClasses(split: List<String>, srgClassName: String, accessWidener: AccessWidener): Boolean {
        val intermediaryClassName = KiltRemapper.remapClass(srgClassName)
        val remapper = KiltRemapper.enhancedRemapper

        // field / method
        if (split.size > 2 && !split[2].startsWith("#")) {
            if (split[2] == "*") { // Handle all of them
                val classInfo = remapper.getClass(srgClassName).orElse(null)

                if (classInfo == null) {
                    logger.warn("Missing class reference (SRG: $srgClassName, Intermediary: $intermediaryClassName) for access transform, skipping.")
                    return false
                }

                for (fieldOpt in classInfo.fields) {
                    val field = fieldOpt.orElse(null) ?: continue

                    accessWidener.visitField(intermediaryClassName, field.mapped, remapper.mapDesc(field.descriptor), AccessWidenerReader.AccessType.ACCESSIBLE, true)
                    accessWidener.visitField(intermediaryClassName, field.mapped, remapper.mapDesc(field.descriptor), AccessWidenerReader.AccessType.MUTABLE, true)
                }

                for (methodOpt in classInfo.methods) {
                    val method = methodOpt.orElse(null) ?: continue

                    accessWidener.visitMethod(intermediaryClassName, method.mapped, remapper.mapMethodDesc(method.descriptor), AccessWidenerReader.AccessType.ACCESSIBLE, true)
                    accessWidener.visitMethod(intermediaryClassName, method.mapped, remapper.mapMethodDesc(method.descriptor), AccessWidenerReader.AccessType.EXTENDABLE, true)
                }
            } else if (split[2].contains("(")) { // method
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

                val mappedDescriptor = KiltRemapper.remapDescriptor(descriptor)

                if (name == "*") {
                    val cls = remapper.getClass(intermediaryClassName).orElse(null) ?: return false

                    for (methodOpt in cls.methods) {
                        methodOpt.ifPresent {
                            if (mappedDescriptor != "()" && it.descriptor != mappedDescriptor)
                                return@ifPresent

                            // write it into Fabric, as otherwise, @Overwrite mixins will not map correctly.
                            accessWidener.visitMethod(intermediaryClassName, it.mapped, mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                            // make sure it's made extendable by default too, as Fabric automatically marks methods as final when made accessible.
                            accessWidener.visitMethod(intermediaryClassName, it.mapped, mappedDescriptor, AccessWidenerReader.AccessType.EXTENDABLE, true)
                        }
                    }

                    return true
                }

                val methodName = remapper.mapMethodName(srgClassName.replace(".", "/"), name, descriptor)

                // write it into Fabric, as otherwise, @Overwrite mixins will not map correctly.
                accessWidener.visitMethod(intermediaryClassName, methodName, mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                // make sure it's made extendable by default too, as Fabric automatically marks methods as final when made accessible.
                accessWidener.visitMethod(intermediaryClassName, methodName, mappedDescriptor, AccessWidenerReader.AccessType.EXTENDABLE, true)
            } else { // field
                val name = split[2]

                val fieldInfo = KiltRemapper.srgIntermediaryMapping.getClass(srgClassName)?.fields?.firstOrNull { it.original == name } ?: return false
                val fieldName = KiltRemapper.enhancedRemapper.mapFieldName(srgClassName.replace(".", "/"), name, fieldInfo.descriptor ?: return false)

                accessWidener.visitField(intermediaryClassName, fieldName, KiltRemapper.remapDescriptor(fieldInfo.descriptor!!), AccessWidenerReader.AccessType.ACCESSIBLE, true)
                accessWidener.visitField(intermediaryClassName, fieldName, KiltRemapper.remapDescriptor(fieldInfo.descriptor!!), AccessWidenerReader.AccessType.MUTABLE, true)
            }
        } else { // class
            accessWidener.visitClass(intermediaryClassName, AccessWidenerReader.AccessType.ACCESSIBLE, true)
            accessWidener.visitClass(intermediaryClassName, AccessWidenerReader.AccessType.EXTENDABLE, true)
        }

        return true
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