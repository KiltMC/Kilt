package xyz.bluspring.kilt.util

import cpw.mods.util.Lazy
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.neoforged.fml.ModLoadingContext
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.KiltLoader
import java.io.File
import java.lang.reflect.Modifier
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

object KiltHelper {
    val launcher = FabricLauncherBase.getLauncher()

    private val cachedForgeClassNodes = Lazy.of { getForgeClassNodesInternal() }
    private var isForgeClassNodesCleared = false

    // normally I'd expect a synchronized map to be faster, but in our case I guess concurrent hash map is faster?
    private val cachedClassValues: MutableMap<OverrideData, ClassValue<Boolean>> = ConcurrentHashMap()

    private fun checkAllElementsMatch(array: Array<*>, array2: Array<*>): Boolean {
        if (array.size != array2.size)
            return false

        for ((i, first) in array.withIndex()) {
            if (first != array2[i])
                return false
        }

        return true
    }

    fun hasMethodOverride(topClass: Class<*>, superClass: Class<*>, methodName: String, vararg methodArgs: Class<*>): Boolean {
        val overrideData = OverrideData(superClass, methodName, methodArgs)
        return this.cachedClassValues.computeIfAbsent(overrideData) {
            object : ClassValue<Boolean>() {
                override fun computeValue(type: Class<*>): Boolean {
                    try {
                        val method = type.getMethod(methodName, *methodArgs)

                        // If the method is declared in the superClass itself (e.g., default interface method),
                        // it's not an override - return false
                        if (method.declaringClass == superClass) {
                            return false
                        }

                        // If a Fabric mod has this method signature but is private, it could cause a game crash.
                        return !Modifier.isPrivate(method.modifiers)
                    } catch (_: Throwable) {
                        return false
                    }
                }
            }
        }.get(topClass)
    }

    fun hasMethodOverrideWithReturnType(topClass: Class<*>, superClass: Class<*>, methodName: String, returnType: Class<*>, vararg methodArgs: Class<*>): Boolean {
        val overrideData = OverrideData(superClass, methodName, methodArgs, returnType)
        return this.cachedClassValues.computeIfAbsent(overrideData) {
            object : ClassValue<Boolean>() {
                override fun computeValue(type: Class<*>): Boolean {
                    try {
                        val methods = type.methods.filter { it.name == methodName && it.parameterTypes.contentEquals(methodArgs) && it.returnType == returnType }

                        if (methods.isEmpty())
                            return false

                        val method = methods.first()

                        // If the method is declared in the superClass itself (e.g., default interface method),
                        // it's not an override - return false
                        if (method.declaringClass == superClass) {
                            return false
                        }

                        // If a Fabric mod has this method signature but is private, it could cause a game crash.
                        return !Modifier.isPrivate(method.modifiers)
                    } catch (_: Throwable) {
                        return false
                    }
                }
            }
        }.get(topClass)
    }

    fun getForgeClassNodes(): List<ClassNode> {
        if (isForgeClassNodesCleared) {
            throw IllegalStateException("NeoForge class nodes have already been cleared!")
        }

        return cachedForgeClassNodes.get()
    }

    fun clearForgeClassNodes() {
        cachedForgeClassNodes.get().clear()
        isForgeClassNodesCleared = true
    }

    fun joinToString(array: Array<String>, separator: String): String {
        return array.joinToString(separator)
    }

    fun <E> mergeNullableCollections(vararg collections: Collection<E>?): Collection<E> {
        val merged = mutableListOf<E>()

        for (collection in collections) {
            if (collection != null) {
                merged.addAll(collection)
            }
        }

        return merged
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
                filesToScan.add(getPath("net/neoforged/neoforge/common/NeoForgeMod.class") ?: return@run)
            }

            // Test environment
            run {
                filesToScan.add(getPath("xyz/bluspring/kilt/test/KiltTesting.class") ?: return@run)
                // Kilt TODO: fix
//                filesToScan.add(getPath("net/minecraftforge/test/LazyOptionalTest.class") ?: return@run)
            }

            filesToScan
        }
    }

    private fun getPath(path: String): Path? {
        val classUrl = launcher.targetClassLoader.getResource(path) ?: return null
        val fullPath = classUrl.path.replace("/$path", "")

        return File(fullPath).toPath()
    }

    private fun getForgeClassNodesInternal(): MutableList<ClassNode> {
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

    @JvmRecord
    private data class OverrideData(
        val superClass: Class<*>,
        val methodName: String,
        val methodArgs: Array<out Class<*>>,
        val returnType: Class<*>? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OverrideData

            if (superClass != other.superClass) return false
            if (methodName != other.methodName) return false
            if (!methodArgs.contentEquals(other.methodArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = superClass.hashCode()
            result = 31 * result + methodName.hashCode()
            result = 31 * result + methodArgs.contentHashCode()
            return result
        }
    }

    // Turns out, there are Forge mods that forcibly exit the game if Kilt or Porting Lib are detected, with zero information provided whatsoever.
    // I don't exactly *want* to argue nor combat this, but because of how hostile this act is, the fact that it directly hinders Kilt development,
    // that the users are provided with zero information as to why their game closed, that if Kilt grows popular enough these mods will not be able
    // to be used alongside Kilt whatsoever, and any further reasons, I kind of have to do this, as a compromise between the mod developer's wishes
    // and me trying to make Kilt even better.
    // See: https://discord.com/channels/1062715218087645215/1373601947155693588, https://github.com/KiltMC/Kilt/issues/75
    @JvmStatic
    fun handleSystemExit(code: Int) {
        Kilt.logger.error("Kilt: A Forge mod has called System.exit() directly from its code! Exit code: $code")

        if (ModLoadingContext.get().activeContainer != null) {
            val container = ModLoadingContext.get().activeContainer
            Kilt.logger.error("Kilt: Active mod container: ${container.modInfo.displayName} (${container.modId})")
        }

        val exception = IllegalStateException("A Forge mod has called System.exit($code) directly! Please check the logs, and report this to the Kilt issues page!")

        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            exception.printStackTrace()
            Kilt.logger.error("Kilt: Because we're in the development environment, we will not exit the game!")

            return
        } else if (KiltFlags.DISABLE_FORGE_SYSTEM_EXIT) {
            exception.printStackTrace()
            Kilt.logger.error("Kilt: Because the user has the disable flag enabled, we will not exit the game!")

            return
        }

        throw exception
    }

    @JvmStatic
    fun createMixinException(injection: Class<*>, methodName: String): RuntimeException {
        return RuntimeException("Go yell at Naz, he forgot to implement ${injection.simpleName}#$methodName")
    }
}