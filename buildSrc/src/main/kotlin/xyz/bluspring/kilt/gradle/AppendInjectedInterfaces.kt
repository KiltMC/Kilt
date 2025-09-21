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

            // Convert the NeoForge injected interfaces first.
            convertNeoInjectedInterfaces(mojToIntermediary)

            // Then try to parse and add our custom injections
            parseAndAddKiltInjections(mojToIntermediary)
        }

        return super.read(cbuf, off, len)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun parseAndAddKiltInjections(mojToIntermediary: Map<String, String>) {
        val fmj = JsonParser.parseReader(this.`in`).asJsonObject
        val fmjInjectedInterfaces = fmj.getAsJsonObject("custom").getAsJsonObject("loom:injected_interfaces")
        val injectionPath = rootProjectDirPath.resolve("src/main/java/xyz/bluspring/kilt/injections")

        for (path in injectionPath.walk()) {
            val relativePath = path.relativeTo(injectionPath)
            val pathName = relativePath.getName(0).name
            var rootPath = "net/minecraft"

            if (pathName == "sodium")
                continue

            if (pathName == "blaze3d" || pathName == "math")
                rootPath = "com/mojang"

            val mcClass = "$rootPath/${relativePath.joinToString("/").replace("Injection.java", "")}"
            val mapped = mojToIntermediary[mcClass] ?: mcClass
            val injected = "xyz/bluspring/kilt/injections/" + relativePath.joinToString("/").replace(".java", "")

            if (mapped == mcClass) {
                if (rootPath == "net/minecraft") {
                    println("Failed to map injected class $mcClass! Skipping...")
                    continue
                }
            }

            if (!fmjInjectedInterfaces.has(mapped)) {
                fmjInjectedInterfaces.add(mapped, JsonArray().apply {
                    this.add(injected)
                })
            } else {
                val existing = fmjInjectedInterfaces.getAsJsonArray(mapped)
                if (existing.any { it.asString == injected })
                    continue

                existing.add(injected)
            }
        }

        this.`in` = StringReader(gson.toJson(fmj))
    }

    private fun convertNeoInjectedInterfaces(mojToIntermediary: Map<String, String>) {
        val neo = JsonParser.parseReader(rootProjectDirPath.resolve("forge/src/main/resources/META-INF/injected-interfaces.json").reader()).asJsonObject
        val fmj = JsonParser.parseReader(this.`in`).asJsonObject

        val fmjInjectedInterfaces = fmj.getAsJsonObject("custom").getAsJsonObject("loom:injected_interfaces")

        for ((mcClass, injected) in neo.entrySet()) {
            val mapped = mojToIntermediary[mcClass] ?: mcClass

            if (mapped == mcClass)
                println("Failing to map injected class $mcClass (got: $mapped)")

            if (!fmjInjectedInterfaces.has(mapped)) {
                fmjInjectedInterfaces.add(mapped, injected)
            } else {
                val existing = fmjInjectedInterfaces.getAsJsonArray(mapped)
                val unique = injected.asJsonArray.filter { !existing.contains(it) }

                for (element in unique) {
                    if (element.asString.contains("<"))
                        if (element.asString.contains("<T>"))
                            existing.add(element.asString.replace("<T>", "<TT;>"))
                        else if (element.asString.contains("<ResourceKey<?>>"))
                            existing.add(element.asString.replace("<ResourceKey<?>>", "<Lnet/minecraft/class_5321;>"))
                        else
                            existing.add(element.asString.replaceAfter("<", "").removeSuffix("<"))
                    else
                        existing.add(element)
                }
            }
        }

        this.`in` = StringReader(gson.toJson(fmj))
        this.hasExpanded = true
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