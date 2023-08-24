package xyz.bluspring.kilt.util

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.KiltLoader
import java.io.File
import java.util.jar.JarFile

object KiltHelper {
    val launcher = FabricLauncherBase.getLauncher()
    private val cachedForgeClassNodes = getForgeClassNodesInternal()

    fun getForgeClassNodes(): List<ClassNode> {
        return cachedForgeClassNodes
    }

    private fun getForgeClassNodesInternal(): List<ClassNode> {
        val list = mutableListOf<ClassNode>()

        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            val kiltFile = File(KiltLoader::class.java.protectionDomain.codeSource.location.toURI())
            val kiltJar = JarFile(kiltFile)

            kiltJar.entries().asIterator().forEach {
                if (it.name.endsWith(".class")) {
                    val inputStream = kiltJar.getInputStream(it)
                    val classReader = ClassReader(inputStream)
                    val classNode = ClassNode(Opcodes.ASM9)
                    classReader.accept(classNode, 0)

                    list.add(classNode)
                }
            }
        } else {
            // Need to do this workaround to scan the Kilt JAR in dev.

            val filesToScan = mutableListOf<File>()

            val kiltClassUrl = launcher.targetClassLoader.getResource("xyz/bluspring/kilt/loader/KiltLoader.class")!!
            val path = kiltClassUrl.path.replace("/xyz/bluspring/kilt/loader/KiltLoader.class", "")
            val kotlinPath = File(path)
            filesToScan.add(kotlinPath)

            val forgeClassUrl = launcher.targetClassLoader.getResource("net/minecraftforge/common/ForgeMod.class")!!
            val forgePath = forgeClassUrl.path.replace("/net/minecraftforge/common/ForgeMod.class", "")
            val forgeFile = File(forgePath)
            filesToScan.add(forgeFile)

            filesToScan.forEach { file ->
                file.walk().forEach {
                    if (it.name.endsWith(".class")) {
                        val inputStream = it.inputStream()
                        val classReader = ClassReader(inputStream)
                        val classNode = ClassNode(Opcodes.ASM9)
                        classReader.accept(classNode, 0)

                        list.add(classNode)
                    }
                }
            }
        }

        return list
    }
}