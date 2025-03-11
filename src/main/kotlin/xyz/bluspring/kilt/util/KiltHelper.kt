package xyz.bluspring.kilt.util

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.KiltLoader
import java.io.File
import java.nio.file.Path
import java.util.jar.JarFile

object KiltHelper {
    val launcher = FabricLauncherBase.getLauncher()
    private val cachedForgeClassNodes = getForgeClassNodesInternal()

    fun getForgeClassNodes(): List<ClassNode> {
        return cachedForgeClassNodes
    }

    fun joinToString(array: Array<String>, separator: String): String {
        return array.joinToString(separator)
    }

    fun isNullOrEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }

    fun getKiltPaths(): List<Path> {
        return if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            //listOf(KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath())
            listOf()
        } else {
            val filesToScan = mutableListOf<Path>()

            // Main environment
            run {
                filesToScan.add(getPath("xyz/bluspring/kilt/loader/KiltLoader.class") ?: return@run)
                filesToScan.add(getPath("net/minecraftforge/common/ForgeMod.class") ?: return@run)
            }

            // Test environment
            run {
                filesToScan.add(getPath("xyz/bluspring/kilt/test/KiltTesting.class") ?: return@run)
                filesToScan.add(getPath("net/minecraftforge/test/LazyOptionalTest.class") ?: return@run)
            }

            filesToScan
        }
    }

    private fun getPath(path: String): Path? {
        val classUrl = launcher.targetClassLoader.getResource(path) ?: return null
        val fullPath = classUrl.path.replace("/$path", "")

        return File(fullPath).toPath()
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

            val filesToScan = getKiltPaths()

            filesToScan.forEach { file ->
                file.toFile().walk().forEach {
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

    // Slightly modified from Lithium: https://github.com/CaffeineMC/lithium/blob/develop/common/src/main/java/net/caffeinemc/mods/lithium/common/reflection/ReflectionUtil.java#L20
    @JvmStatic
    fun hasMethodOverride(clazz: Class<*>, superClass: Class<*>, methodName: String, vararg methodArgs: Class<*>): Boolean {
        var current: Class<*>? = clazz

        while (current != null && current != superClass && superClass.isAssignableFrom(current)) {
            try {
                clazz.getDeclaredMethod(methodName, *methodArgs)
                return true
            } catch (_: NoSuchMethodException) {
                current = current.superclass
            } catch (_: Throwable) {
                return false
            }
        }

        return false
    }
}