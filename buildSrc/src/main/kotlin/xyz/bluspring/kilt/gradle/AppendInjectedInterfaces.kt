package xyz.bluspring.kilt.gradle

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.File

class AppendInjectedInterfaces {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    fun convertNeoToFMJ(neo: JsonObject, fmj: JsonObject, version: String, tempDir: File) {
        val mappingDownloader = MappingDownloader(version, tempDir)
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
                        else
                            existing.add(element.asString.replaceAfter("<", "").removeSuffix("<"))
                    else
                        existing.add(element)
                }
            }
        }
    }
}