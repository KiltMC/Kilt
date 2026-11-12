package xyz.bluspring.kilt.gradle

import com.google.gson.JsonParser
import net.minecraftforge.installer.SimpleInstaller
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import java.util.jar.JarFile
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

object LocalVarMappingBuilder {
    private val logger = LoggerFactory.getLogger(LocalVarMappingBuilder::class.java)

    data class LocalInfo(val type: Type, val ordinal: Int, val isParam: Boolean)

    @OptIn(ExperimentalAtomicApi::class)
    fun setupNeo(project: Project) {
        val profile = JsonParser.parseString(SimpleInstaller::class.java.getResource("/install_profile.json")!!.readText()).asJsonObject
        val neoVersion = profile.get("version").asString.split("-")[1]
        val neoFormVersion = profile.getAsJsonObject("data").getAsJsonObject("MCP_VERSION").get("client").asString.removeSurrounding("'")
        val installPath = project.file(".gradle/kilt-cache/neoforge-$neoVersion/")

        val neoJar = installPath.resolve("libraries/net/minecraft/client/$neoFormVersion/client-$neoFormVersion-srg.jar")

        if (!neoJar.exists()) {
            installPath.mkdirs()

            // create fake MC metadata to trick NeoForge's installer into installing the client.
            installPath.resolve("launcher_profiles.json").apply {
                createNewFile()
                writeText("{}")
            }

            SimpleInstaller.main(arrayOf("--installClient", installPath.absolutePath))
        }

        if (!neoJar.exists())
            throw IllegalStateException("Where the fuck did Neo go?")

        // okay, time to grab the LVTs.
        // map: class name -> ((method name, method desc) -> (local name, local var info))
        val localVars = mutableMapOf<String, Map<Pair<String, String>, Map<String, LocalInfo>>>()
        val neoJarFile = JarFile(neoJar)

        neoJarFile.stream().forEach { entry ->
            if (!entry.name.endsWith(".class"))
                return@forEach

            val classReader = ClassReader(neoJarFile.getInputStream(entry))
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            val methodVars = mutableMapOf<Pair<String, String>, Map<String, LocalInfo>>()

            for (methodNode in classNode.methods) {
                if (methodNode.localVariables == null) continue

                val params = Type.getArgumentTypes(methodNode.desc)

                val localVars = mutableMapOf<String, LocalInfo>()
                val localTypeCounts = mutableMapOf<String, AtomicInt>()
                val funnyLocalCounts = mutableMapOf<String, AtomicInt>()

                for (localVar in /*getLocalsAt(classNode, methodNode)*/ methodNode.localVariables.sortedBy { it.index }) {
                    val type = Type.getType(localVar.desc)
                    var localName = localVar.name

                    if (localName.startsWith("$$") && localName.removePrefix("$$").toIntOrNull() != null) {
                        val funnyOrdinal = funnyLocalCounts.computeIfAbsent(localVar.desc) { AtomicInt(0) }.fetchAndIncrement()

                        // follow the same formatting that Neo follows
                        localName = if (type.sort == Type.OBJECT) {
                            type.className.split(".").last().lowercase()
                        } else {
                            type.descriptor.lowercase()
                        }

                        if (funnyOrdinal > 0)
                            localName = "$localName$funnyOrdinal"
                    }

                    if (localVars.contains(localName)) {
                        logger.warn("Duplicate local variable detected: ${classNode.name}#${methodNode.name}${methodNode.desc}: ${localName}:${localVar.desc}")
                        continue
                    }

                    val offset = if (Modifier.isStatic(methodNode.access)) 0 else 1

                    if (offset == 1 && localVar.index == 0 && localName == "this")
                        continue

                    val ordinal = localTypeCounts.computeIfAbsent(localVar.desc) { AtomicInt(0) }.fetchAndIncrement()
                    val isParam = params.lastIndex >= (localVar.index - offset)

                    localVars[localName] = LocalInfo(type, ordinal, isParam)
                }

                if (localVars.isNotEmpty())
                    methodVars[methodNode.name to methodNode.desc] = localVars
            }

            if (methodVars.isNotEmpty())
                localVars[classNode.name] = methodVars
        }

        var lvtData = ""

        for ((className, methodVars) in localVars) {
            if (methodVars.isEmpty())
                continue

            lvtData += "c\t$className\n"

            for ((methodSig, locals) in methodVars) {
                val (methodName, methodDesc) = methodSig
                lvtData += "\tm\t$methodName\t$methodDesc\n"
                for ((localName, local) in locals) {
                    lvtData += "\t\t${if (local.isParam) "p" else "l"}\t$localName\t${local.type.descriptor}\t${local.ordinal}\n"
                }
            }
        }

        val lvtFile = project.file("src/main/resources/local_variable_mappings.txt")
        if (!lvtFile.exists())
            lvtFile.createNewFile()

        lvtFile.writeText(lvtData)
    }
}
