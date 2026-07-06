package xyz.bluspring.kilt.gradle

import com.google.gson.JsonParser
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.net.URL
import java.util.jar.JarFile

class MappingDownloader(private val version: String, private val tempDir: File) {
    val minecraftJarFile = File(tempDir, "minecraft_$version.jar")

    data class NamedPair(
        val name: String,
        val descriptor: String,
        val isFinal: Boolean
    )

    data class ClassMapping(
        val fields: List<NamedPair>,
        val methods: List<NamedPair>
    )

    fun setupMappings(): Map<String, ClassMapping> {
        val startTime = System.currentTimeMillis()
        println("Downloading mapping files...")

        downloadMojangMappings()

        val classMappings = mutableMapOf<String, ClassMapping>()

        val jarFile = JarFile(minecraftJarFile)
        for (entry in jarFile.entries().iterator()) {
            if (entry.name.endsWith(".class")) {
                val classReader = jarFile.getInputStream(entry).use { ClassReader(it) }
                val classNode = ClassNode()
                classReader.accept(classNode, 0)

                val fields = mutableListOf<NamedPair>()
                val methods = mutableListOf<NamedPair>()

                for (fieldNode in classNode.fields) {
                    fields.add(NamedPair(fieldNode.name, fieldNode.desc, fieldNode.access and Opcodes.ACC_FINAL != 0))
                }

                for (methodNode in classNode.methods) {
                    methods.add(NamedPair(methodNode.name, methodNode.desc, (methodNode.access and Opcodes.ACC_FINAL != 0) || (methodNode.access and Opcodes.ACC_STATIC != 0)))
                }

                classMappings[classNode.name] = ClassMapping(fields, methods)
            }
        }

        println("Downloaded mapping files! (took ${System.currentTimeMillis() - startTime}ms)")

        return classMappings
    }

    fun downloadMojangMappings() {
        if (minecraftJarFile.exists()){
            println("Minecraft JAR for $version already exists, skipping.")
            return
        }

        println("Downloading Minecraft JAR for $version...")
        val manifestUrl = URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
        val manifestJson = JsonParser.parseString(manifestUrl.readText()).asJsonObject

        val versionManifestJson = manifestJson.getAsJsonArray("versions").firstOrNull {
            it.asJsonObject.get("id").asString == version
        }?.asJsonObject ?: throw IllegalArgumentException("Invalid version!")

        val versionUrl = URL(versionManifestJson.get("url").asString)
        val versionJson = JsonParser.parseString(versionUrl.readText()).asJsonObject

        val downloads = versionJson.getAsJsonObject("downloads")
        val mappingsUrl = URL(downloads.getAsJsonObject("client").get("url").asString)

        minecraftJarFile.createNewFile()
        minecraftJarFile.writeBytes(mappingsUrl.readBytes())

        println("Minecraft JAR for $version has been downloaded!")
    }
}
