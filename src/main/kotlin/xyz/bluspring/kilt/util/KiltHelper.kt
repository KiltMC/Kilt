package xyz.bluspring.kilt.util

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
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile

object KiltHelper {
    val launcher = FabricLauncherBase.getLauncher()
    private val cachedForgeClassNodes = getForgeClassNodesInternal()
    private val cachedHasMethodOverride = Collections.synchronizedSet(CacheSet<OverrideData>())
    private val cachedHasNoMethodOverride = Collections.synchronizedSet(CacheSet<OverrideData>())

    private fun checkAllElementsMatch(array: Array<*>, array2: Array<*>): Boolean {
        if (array.size != array2.size)
            return false

        for ((i, first) in array.withIndex()) {
            if (first != array2[i])
                return false
        }

        return true
    }

    private fun checkCache(cache: MutableCollection<OverrideData>, topClass: Class<*>, superClass: Class<*>, methodName: String, methodArgs: List<Class<*>>): Boolean {
        synchronized(cache) {
            // Don't use any or first, this will result in *heavy* performance loss.
            val overrideData = OverrideData(topClass, superClass, methodName, methodArgs)

            return cache.contains(overrideData).apply {
                if (this)
                    // Force the cache to mark this as accessed
                    cache.add(overrideData)
            }
        }
    }

    // Modified from Lithium: https://github.com/CaffeineMC/lithium/blob/develop/common/src/main/java/net/caffeinemc/mods/lithium/common/reflection/ReflectionUtil.java#L20
    fun hasMethodOverride(topClass: Class<*>, superClass: Class<*>, methodName: String, vararg methodArgs: Class<*>): Boolean {
        if (checkCache(cachedHasMethodOverride, topClass, superClass, methodName, methodArgs.toList()))
            return true

        if (checkCache(cachedHasNoMethodOverride, topClass, superClass, methodName, methodArgs.toList()))
            return false

        val overrideData = OverrideData(topClass, superClass, methodName, methodArgs.toList())

        var currentClass: Class<*>? = topClass
        while (currentClass != null && currentClass != superClass && currentClass != Object::class.java && superClass.isAssignableFrom(topClass)) {
            try {
                currentClass.getDeclaredMethod(methodName, *methodArgs)
                cachedHasMethodOverride.add(overrideData)
                return true
            } catch (_: NoSuchMethodException) {
                currentClass = currentClass.superclass
            } catch (_: Throwable) {
                cachedHasNoMethodOverride.add(overrideData)
                return false
            }
        }

        cachedHasNoMethodOverride.add(overrideData)
        return false
    }

    fun getForgeClassNodes(): List<ClassNode> {
        return cachedForgeClassNodes
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

    private data class OverrideData(
        val topClass: Class<*>,
        val superClass: Class<*>,
        val methodName: String,
        val methodArgs: List<Class<*>>
    )

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
}