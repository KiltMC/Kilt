package xyz.bluspring.kilt.gradle

import com.google.gson.JsonParser
import java.io.File
import java.net.URL

class MappingDownloader(private val version: String, private val tempDir: File) {
    val mojangMappingsFile = File(tempDir, "mojang_$version.txt")
    val srgMappingsFile = File(tempDir, "srg_$version.tsrg")

    fun downloadFiles() {
        val startTime = System.currentTimeMillis()
        println("Downloading mapping files...")

        downloadMojangMappings()
        downloadSrgMappings()

        println("Downloaded mapping files! (took ${System.currentTimeMillis() - startTime}ms)")
    }

    fun downloadMojangMappings() {
        if (mojangMappingsFile.exists()){
            println("Mojang mappings for $version already exists, skipping.")
            return
        }

        println("Downloading Mojang mappings for $version...")
        val manifestUrl = URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
        val manifestJson = JsonParser.parseString(manifestUrl.readText()).asJsonObject

        val versionManifestJson = manifestJson.getAsJsonArray("versions").firstOrNull {
            it.asJsonObject.get("id").asString == version
        }?.asJsonObject ?: throw IllegalArgumentException("Invalid version!")

        val versionUrl = URL(versionManifestJson.get("url").asString)
        val versionJson = JsonParser.parseString(versionUrl.readText()).asJsonObject

        val downloads = versionJson.getAsJsonObject("downloads")
        val mappingsUrl = URL(downloads.getAsJsonObject("client_mappings").get("url").asString)

        mojangMappingsFile.createNewFile()
        mojangMappingsFile.writeText(mappingsUrl.readText())

        println("Mojang mappings for $version has been downloaded!")
    }

    fun downloadSrgMappings() {
        if (srgMappingsFile.exists()){
            println("SRG mappings for $version already exists, skipping.")
            return
        }

        println("Downloading SRG mappings for $version...")

        val versionType = if (version.contains("pre") || version.contains("rc"))
            "pre"
        else "release" // don't bother handling snapshots, MCP's pretty much never updated for those.

        // This is the most reliable spot where we can get updated stuff, since the Forge Maven is literally never updated for
        // patch version.
        val url = URL("https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/$versionType/$version/joined.tsrg")

        srgMappingsFile.createNewFile()
        srgMappingsFile.writeText(url.readText())
        println("SRG mappings for $version has been downloaded!")
    }
}
