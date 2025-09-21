package xyz.bluspring.kilt.gradle

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCopyDetails
import java.io.File
import java.io.FilterReader
import java.io.Reader
import java.io.StringReader
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.joinToString
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.name
import kotlin.io.path.reader
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

class AppendInjectedInterfaces(reader: Reader) : FilterReader(reader) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    private var hasExpanded = false

    private lateinit var projectDir: String
    private lateinit var rootProjectDir: String
    private lateinit var version: String
    private lateinit var tempDir: String

    private lateinit var projectDirPath: Path
    private lateinit var rootProjectDirPath: Path
    private lateinit var tempDirPath: Path

    override fun read(cbuf: CharArray?, off: Int, len: Int): Int {
        if (!hasExpanded) {
            validateParams()

            val mappingDownloader = MappingDownloader(version, tempDirPath.toFile())
            mappingDownloader.downloadFiles()

            val intermediary = MemoryMappingTree() // obf -> intermediary
            MappingReader.read(mappingDownloader.intermediaryMappingsFile.reader(),  intermediary)

            val mojmap = MemoryMappingTree() // obf -> moj
            MappingReader.read(mappingDownloader.mojangMappingsFile.reader(), mojmap)

            println("Mapping MojMap to Intermediary classes...")
            val mojToIntermediary = mutableMapOf<String, String>()

            for (classMapping in mojmap.classes) {
                val intermediaryClassMap = intermediary.classes.firstOrNull { it.getName("official") == classMapping.getName(0) } ?: continue

                try {
                    mojToIntermediary[classMapping.srcName] = intermediaryClassMap.getName("intermediary")!!
                } catch (e: Throwable) {
                    println("Failed to map ${classMapping.getName(0)} / ${intermediaryClassMap.getName("intermediary")}")
                    e.printStackTrace()
                }
            }

            val injected = mutableMapOf<String, MutableList<String>>()

            // Convert the NeoForge injected interfaces first.
            for ((sourceClass, injections) in convertNeoInjectedInterfaces(rootProjectDirPath)) {
                injected.computeIfAbsent(sourceClass) { mutableListOf() }.addAll(injections)
            }

            // Then try to parse and add our custom injections
            for ((sourceClass, injectionClass) in parseAndAddKiltInjections(rootProjectDirPath)) {
                injected.computeIfAbsent(sourceClass) { mutableListOf() }.add(injectionClass)
            }

            // Now we write it to the FMJ
            val fmj = JsonParser.parseReader(this.`in`).asJsonObject
            val fmjInjectedInterfaces = fmj.getAsJsonObject("custom").getAsJsonObject("loom:injected_interfaces")

            for ((className, injections) in injected) {
                fmjInjectedInterfaces.add((mojToIntermediary[className] ?: className).replace("$", "\\u0024"), JsonArray().apply {
                    for (string in injections) {
                        this.add(string.replace("$", "\\u0024"))
                    }
                })
            }

            this.`in` = StringReader(gson.toJson(fmj))
            this.hasExpanded = true
        }

        return super.read(cbuf, off, len)
    }

    companion object {
        @OptIn(ExperimentalPathApi::class)
        fun parseAndAddKiltInjections(rootPath: Path): Map<String, String> {
            val map = mutableMapOf<String, String>()
            val injectionPath = rootPath.resolve("src/main/java/xyz/bluspring/kilt/injections")

            for (path in injectionPath.walk()) {
                val relativePath = path.relativeTo(injectionPath)
                val pathName = relativePath.getName(0).name
                var rootPath = "net/minecraft"

                if (pathName == "sodium")
                    continue

                if (pathName == "blaze3d" || pathName == "math")
                    rootPath = "com/mojang"

                val mcClass = "$rootPath/${relativePath.joinToString("/").replace("Injection.java", "")}"
                val injected = "xyz/bluspring/kilt/injections/" + relativePath.joinToString("/").replace(".java", "")
                map[mcClass] = injected
            }

            return map
        }

        fun convertNeoInjectedInterfaces(rootPath: Path): Map<String, List<String>> {
            val map = mutableMapOf<String, List<String>>()
            val neo = JsonParser.parseReader(rootPath.resolve("forge/src/main/resources/META-INF/injected-interfaces.json").reader()).asJsonObject

            for ((mcClass, injected) in neo.entrySet()) {
                map[mcClass] = injected.asJsonArray.map { element ->
                    if (element.asString.contains("<"))
                        if (element.asString.contains("<T>"))
                            element.asString.replace("<T>", "<TT;>")
                        else if (element.asString.contains("<ResourceKey<?>>"))
                            element.asString.replace("<ResourceKey<?>>", "<Lnet/minecraft/class_5321;>")
                        else
                            element.asString.replaceAfter("<", "").removeSuffix("<")
                    else
                        element.asString
                }.toList()
            }

            return map
        }
    }

    private fun validateParams() {
        this.projectDirPath = Paths.get(this.projectDir)
        this.rootProjectDirPath = Paths.get(this.rootProjectDir)
        this.tempDirPath = Paths.get(this.tempDir)
    }

    data class Applicator(val project: Project) : Action<FileCopyDetails> {
        override fun execute(details: FileCopyDetails) {
            val params = mapOf(
                "projectDir" to project.layout.projectDirectory.asFile.toPath().toString(),
                "rootProjectDir" to project.rootProject.layout.projectDirectory.asFile.toPath().toString(),
                "tempDir" to project.layout.buildDirectory.get().asFile.toPath().toString(),
                "version" to project.property("minecraft_version") as String
            )

            details.filter(params, AppendInjectedInterfaces::class.java)
        }
    }
}