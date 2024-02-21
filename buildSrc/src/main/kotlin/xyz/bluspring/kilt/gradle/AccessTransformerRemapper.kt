package xyz.bluspring.kilt.gradle

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.File

class AccessTransformerRemapper {
    fun remapDescriptor(descriptor: String, mappings: MemoryMappingTree): String {
        var formed = ""

        var incomplete = ""
        var inClass = false
        for (c in descriptor) {
            if (c == 'L' && !inClass)
                inClass = true

            if (inClass) {
                incomplete += c

                if (c == ';') {
                    inClass = false
                    formed += 'L'

                    val name = incomplete.removePrefix("L").removeSuffix(";")
                    formed += mappings.classes.firstOrNull { it.getName(0) == name }?.srcName ?: name

                    formed += ';'

                    incomplete = ""
                }
            } else {
                formed += c
            }
        }

        return formed
    }

    fun convertTransformerToWidener(data: String, output: File, version: String, tempDir: File) {
        val mappingDownloader = MappingDownloader(version, tempDir)
        mappingDownloader.downloadFiles()

        val srg = MemoryMappingTree() // obf -> srg
        MappingReader.read(mappingDownloader.srgMappingsFile.reader(), srg)

        val mojmap = MemoryMappingTree() // obf -> moj
        MappingReader.read(mappingDownloader.mojangMappingsFile.reader(), mojmap)

        println("Mapping SRG directly to MojMap...")
        val srg2mojmap = mutableMapOf<String, String>()
        val fieldDescriptors = mutableMapOf<String, String>()

        for (classMapping in srg.classes) {
            val mojClassMap = mojmap.classes.firstOrNull { it.getName(0) == classMapping.srcName } ?: continue

            for (field in classMapping.fields) {
                val srgName = field.getName("srg")!!
                if (!srgName.startsWith("f_") && !srgName.startsWith("m_")) {
                    continue
                }

                val mojField = mojClassMap.fields.firstOrNull { it.getName(0) == field.srcName } ?: continue
                srg2mojmap[srgName] = mojField.srcName
                if (mojField.srcDesc != null)
                    fieldDescriptors[srgName] = mojField.srcDesc!!
            }

            for (method in classMapping.methods) {
                val srgName = method.getName("srg")!!
                if (!srgName.startsWith("f_") && !srgName.startsWith("m_"))
                    continue

                val remappedDesc = remapDescriptor(method.srcDesc ?: "", mojmap)
                val mojMethod = mojClassMap.methods.firstOrNull { it.getName(0) == method.srcName && remappedDesc == it.srcDesc } ?: continue
                srg2mojmap[srgName] = mojMethod.srcName
            }
        }

        println("Finished mapping SRG to MojMap!")
        println("Proceeding with converting access transformer to access widener...")

        val widener = mutableListOf<String>()

        widener += "accessWidener v2 named"
        widener += "# Auto generated access widener from NeoForge's access transformers."

        for (line in data.lines()) {
            val trimmed = line.replaceAfter("#", "").replace("#", "").trim()

            if (trimmed.isBlank())
                continue

            val split = trimmed.split(" ")

            val className = split[1].replace(".", "/")

            if (split.size == 2) { // Class
                widener += "transitive-accessible class $className"
                widener += "transitive-extendable class $className"
            } else {
                if (split[2].contains("(")) { // Method
                    val srgMethodName = split[2].replaceAfter("(", "").replace("(", "")
                    val methodName = srg2mojmap[srgMethodName] ?: srgMethodName

                    // this isn't a joke, why does Forge access transform lambdas????
                    if (methodName.startsWith("lambda$"))
                        continue

                    val descriptor = split[2].replaceBefore("(", "")
                    widener += "transitive-accessible method $className $methodName $descriptor"
                    widener += "transitive-extendable method $className $methodName $descriptor"
                } else { // Field
                    val srgFieldName = split[2]
                    val fieldName = srg2mojmap[srgFieldName] ?: srgFieldName
                    val descriptor = fieldDescriptors[srgFieldName] ?: "# TODO: ADD DESC"

                    widener += "transitive-accessible field $className $fieldName $descriptor"
                    widener += "transitive-mutable field $className $fieldName $descriptor"
                }
            }
        }

        // Custom widener values for Kilt
        widener += "transitive-accessible class net/minecraft/world/item/CreativeModeTab\$ItemDisplayBuilder"

        if (!output.exists())
            output.createNewFile()

        output.writeText(widener.joinToString("\n"))
    }
}