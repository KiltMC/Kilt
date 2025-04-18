package xyz.bluspring.kilt.loader.asm

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.lib.accesswidener.AccessWidenerReader
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.util.regex.Pattern

// A reimplementation of Forge's Access Transformers.
// The specification can be found here: https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md
object AccessTransformerLoader {
    private val logger = LoggerFactory.getLogger("Kilt Access Transformers")
    private val debug = KiltFlags.ENABLE_ACCESS_TRANSFORMER_DEBUG

    private val whitespace = Pattern.compile("[ \t]+")

    private val mappingResolver = FabricLoader.getInstance().mappingResolver

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
                val intermediaryClassName = KiltRemapper.remapClass(srgClassName)

                // field / method
                if (split.size > 2 && !split[2].startsWith("#")) {
                    if (split[2] == "*") { // Handle all of them
                        val classInfo = KiltRemapper.srgIntermediaryMapping.getClass(srgClassName)

                        for (field in classInfo.fields) {
                            accessWidener.visitField(intermediaryClassName, field.mapped, field.mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                            accessWidener.visitField(intermediaryClassName, field.mapped, field.mappedDescriptor, AccessWidenerReader.AccessType.MUTABLE, true)
                        }

                        for (method in classInfo.methods) {
                            accessWidener.visitMethod(intermediaryClassName, method.mapped, method.mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                            accessWidener.visitMethod(intermediaryClassName, method.mapped, method.mappedDescriptor, AccessWidenerReader.AccessType.EXTENDABLE, true)
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
                        val methodName = KiltRemapper.enhancedRemapper.mapMethodName(srgClassName.replace(".", "/"), name, descriptor)

                        // write it into Fabric, as otherwise, @Overwrite mixins will not map correctly.
                        accessWidener.visitMethod(intermediaryClassName, methodName, mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                        // make sure it's made extendable by default too, as Fabric automatically marks methods as final when made accessible.
                        accessWidener.visitMethod(intermediaryClassName, methodName, mappedDescriptor, AccessWidenerReader.AccessType.EXTENDABLE, true)
                    } else { // field
                        val name = split[2]

                        val fieldInfo = KiltRemapper.srgIntermediaryMapping.getClass(srgClassName)?.fields?.firstOrNull { it.original == name } ?: continue
                        val fieldName = KiltRemapper.enhancedRemapper.mapFieldName(srgClassName.replace(".", "/"), name, fieldInfo.descriptor ?: continue)

                        accessWidener.visitField(intermediaryClassName, fieldName, fieldInfo.mappedDescriptor, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                        accessWidener.visitField(intermediaryClassName, fieldName, fieldInfo.mappedDescriptor, AccessWidenerReader.AccessType.MUTABLE, true)
                    }
                } else { // class
                    accessWidener.visitClass(intermediaryClassName, AccessWidenerReader.AccessType.ACCESSIBLE, true)
                    accessWidener.visitClass(intermediaryClassName, AccessWidenerReader.AccessType.EXTENDABLE, true)
                }
            } catch (e: Throwable) {
                throw RuntimeException("Failed to process access transformer at line ${index + 1}: \"$line\"", e)
            }
        }
    }
}