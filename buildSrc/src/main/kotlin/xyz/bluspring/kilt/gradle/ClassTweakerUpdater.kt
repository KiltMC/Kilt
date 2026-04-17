package xyz.bluspring.kilt.gradle

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Project
import java.io.File
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.name

object ClassTweakerUpdater {
    private fun StringBuilder.newLine(): StringBuilder = this.append("\n")

    private fun File.hash(algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(this.readBytes())
        return HexFormat.of().formatHex(hash).lowercase()
    }

    fun updateTweakers(project: Project) {
        val lastUpdatedFile = File("${project.projectDir}/tweakers/last_updated.json")
        val json = JsonParser.parseReader(lastUpdatedFile.reader()).asJsonObject

        val lastTransformerHash = if (json.has("wideners") && json.getAsJsonObject("wideners").has("neoforge"))
            json.getAsJsonObject("wideners").get("neoforge").asString
        else null

        val lastInjectionHash = if (json.has("injections") && json.getAsJsonObject("injections").has("neoforge"))
            json.getAsJsonObject("injections").get("neoforge").asString
        else null

        // Load Mojmap mappings
        val mappingDownloader = MappingDownloader(project.property("minecraft_version") as String, project.layout.buildDirectory.get().asFile)
        mappingDownloader.downloadFiles()

        val mojmap = MemoryMappingTree() // obf -> moj
        MappingReader.read(mappingDownloader.mojangMappingsFile.reader(), mojmap)

        // Tweakers
        val kiltGeneratedTweaker = File("${project.projectDir}/tweakers/injections/kilt_generated.classtweaker")
        val overrideTweakerFile = File("${project.projectDir}/tweakers/injections/kilt.classtweaker")
        val kiltWidenerFile = File("${project.projectDir}/tweakers/wideners/kilt.accesswidener")
        val neoInjectionTweakerFile = File("${project.projectDir}/tweakers/injections/neoforge.classtweaker")
        val neoWidenerFile = File("${project.projectDir}/tweakers/wideners/neoforge.accesswidener")

        val finalTweakerFile = File("${project.projectDir}/src/main/resources/kilt.classtweaker")

        // Neo-specific files
        run {
            val transformerFile = File("${project.projectDir}/forge/src/main/resources/META-INF/accesstransformer.cfg")
            val injectionsFile = File("${project.projectDir}/forge/src/main/resources/META-INF/injected-interfaces.json")

            // Access Transformer -> Access Widener Updates
            if (transformerFile.hash() != lastTransformerHash) {
                convertTransformerToWidener(transformerFile, neoWidenerFile, mojmap)
                json.getAsJsonObject("wideners").addProperty("neoforge", transformerFile.hash())
            }

            // Injected Interface Updates
            if (injectionsFile.hash() != lastInjectionHash) {
                updateNeoInjections(injectionsFile, neoInjectionTweakerFile)
                json.getAsJsonObject("injections").addProperty("neoforge", injectionsFile.hash())
            }
        }

        // Kilt-specific files
        run {
            val injectionsPath = File("${project.projectDir}/src/main/java/xyz/bluspring/kilt/injections")

            // We don't want to overwrite our manual injections with the auto-generated variants.
            val existingInjections = overrideTweakerFile.readLines()
                .filter { it.trim().startsWith("transitive-inject-interface") || it.trim().startsWith("inject-interface") }
                .map {
                    it.split(" ")[2]
                        // Any custom generics will not be compared properly, so we should just strip them out in the check.
                        .replaceAfter("<", "").removeSuffix("<")
                }

            // Automatically generate most of our injections.
            updateKiltInjections(injectionsPath, kiltGeneratedTweaker, mojmap, existingInjections)
        }

        // Now, we should combine everything together into one shared class tweaker.
        val classTweakerBuilder = StringBuilder()
        classTweakerBuilder.append("classTweaker v1 named")
        classTweakerBuilder.newLine()

        // Neo ATs
        neoWidenerFile.forEachLine {
            if (it.startsWith("accessWidener")) return@forEachLine
            classTweakerBuilder.append(it).newLine()
        }

        // Neo Injections
        neoInjectionTweakerFile.forEachLine {
            if (it.startsWith("classTweaker")) return@forEachLine
            classTweakerBuilder.append(it).newLine()
        }

        // Kilt AWs
        kiltWidenerFile.forEachLine {
            if (it.startsWith("accessWidener")) return@forEachLine
            classTweakerBuilder.append(it).newLine()
        }

        // Kilt Injections
        overrideTweakerFile.forEachLine {
            if (it.startsWith("classTweaker")) return@forEachLine
            classTweakerBuilder.append(it).newLine()
        }

        // Kilt Injections (Generated)
        kiltGeneratedTweaker.forEachLine {
            if (it.startsWith("classTweaker")) return@forEachLine
            classTweakerBuilder.append(it).newLine()
        }

        // Write it into the final tweaker file
        if (!finalTweakerFile.exists())
            finalTweakerFile.createNewFile()

        finalTweakerFile.writeText(classTweakerBuilder.toString())

        // Also updated the last updated
        lastUpdatedFile.writeText(GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json))
    }

    fun convertTransformerToWidener(transformer: File, output: File, mojmap: MemoryMappingTree) {
        val fieldDescriptors = mutableMapOf<String, MutableMap<String, String>>()

        for (classMapping in mojmap.classes) {
            for (field in classMapping.fields) {
                val mojName = field.srcName

                if (field.srcDesc != null)
                    fieldDescriptors.computeIfAbsent(classMapping.srcName) { mutableMapOf() }[mojName] = field.srcDesc!!
            }
        }

        val widener = mutableListOf<String>()

        widener += "accessWidener v2 named"
        widener += ""
        widener += "# Auto generated access widener from NeoForge's access transformers."

        for (line in transformer.readLines()) {
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
                    val methodName = split[2].replaceAfter("(", "").replace("(", "")

                    // this isn't a joke, why does Forge access transform lambdas????
                    if (methodName.startsWith("lambda$"))
                        continue

                    val descriptor = split[2].replaceBefore("(", "")
                    widener += "transitive-accessible method $className $methodName $descriptor"
                    widener += "transitive-extendable method $className $methodName $descriptor"
                } else { // Field
                    val fieldName = split[2]
                    val descriptor = fieldDescriptors[className]?.get(fieldName) ?: "# TODO: ADD DESC"

                    val prefix = if (descriptor.contains("# TODO: ")) "# " else ""
                    widener += "${prefix}transitive-accessible field $className $fieldName $descriptor"
                    widener += "${prefix}transitive-mutable field $className $fieldName $descriptor"
                }
            }
        }

        // Custom widener values for Kilt
        widener += "transitive-accessible class net/minecraft/world/item/CreativeModeTab\$ItemDisplayBuilder"
        widener += "transitive-accessible class net/minecraft/client/gui/screens/advancements/AdvancementTabType"
        widener += "transitive-accessible field net/minecraft/client/renderer/ItemBlockRenderTypes TYPE_BY_BLOCK Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/client/renderer/ItemBlockRenderTypes TYPE_BY_FLUID Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/commands/synchronization/ArgumentTypeInfos BY_CLASS Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/world/entity/SpawnPlacements DATA_BY_TYPE Ljava/util/Map;"
        widener += "transitive-accessible class net/minecraft/world/entity/SpawnPlacements\$Data"
        widener += "transitive-accessible class net/minecraft/core/registries/BuiltInRegistries\$RegistryBootstrap"
        widener += "transitive-accessible class net/minecraft/core/RegistrySetBuilder\$BuildState"
        widener += "transitive-accessible class net/minecraft/core/RegistrySetBuilder\$BuildState\$1"

        if (!output.exists())
            output.createNewFile()

        output.writeText(widener.joinToString("\n"))
    }

    fun updateNeoInjections(injections: File, tweaker: File) {
        val neo = JsonParser.parseReader(injections.reader()).asJsonObject
        val classTweakerBuilder = StringBuilder()
        classTweakerBuilder.append("classTweaker v1 named")
        classTweakerBuilder.newLine()
        classTweakerBuilder.append("# NeoForge interface injections, automatically generated from their injected-interfaces.json")
        classTweakerBuilder.newLine()

        for ((mcClass, injected) in neo.entrySet()) {
            for (element in injected.asJsonArray) {
                val injected = if (element.asString.contains("<"))
                    if (element.asString.contains("<T>"))
                        element.asString.replace("<T>", "<TT;>")
                    else if (element.asString.contains("<ResourceKey<?>>")) // Why is it designed this way?
                        element.asString.replace("<ResourceKey<?>>", "<Lnet/minecraft/resources/ResourceKey<*>;>")
                    else if (element.asString.contains("<Object>")) // Why is it designed this way?
                        element.asString.replace("<Object>", "<Ljava/lang/Object;>")
                    else
                        element.asString.replaceAfter("<", "").removeSuffix("<")
                else
                    element.asString
                classTweakerBuilder.append("transitive-inject-interface $mcClass $injected")
                    .newLine()
            }
        }

        if (!tweaker.exists())
            tweaker.createNewFile()

        tweaker.writeText(classTweakerBuilder.toString())
    }

    fun updateKiltInjections(injectionPath: File, tweaker: File, mojmap: MemoryMappingTree, existing: List<String>) {
        val classTweakerBuilder = StringBuilder()
        classTweakerBuilder.append("classTweaker v1 named")
        classTweakerBuilder.newLine()
        classTweakerBuilder.append("")

        for (path in injectionPath.walk()) {
            if (path.isDirectory)
                continue

            val relativePath = path.relativeTo(injectionPath).toPath()
            val pathName = relativePath.getName(0).name
            var rootPath = "net/minecraft"

            if (pathName == "sodium")
                continue

            if (pathName == "blaze3d" || pathName == "math")
                rootPath = "com/mojang"

            val mcClass = "$rootPath/${relativePath.joinToString("/").replace("Injection.java", "")}"
            val injected = "xyz/bluspring/kilt/injections/" + relativePath.joinToString("/").replace(".java", "")

            if (existing.contains(injected))
                continue

            if (mojmap.getClass(mcClass) == null) {
                println("Failed to locate class mapping for $injected! Make sure it's properly inserted in the manual injections!")
                continue
            }

            classTweakerBuilder.append("transitive-inject-interface $mcClass $injected").newLine()
        }

        if (!tweaker.exists())
            tweaker.createNewFile()

        tweaker.writeText(classTweakerBuilder.toString())
    }
}