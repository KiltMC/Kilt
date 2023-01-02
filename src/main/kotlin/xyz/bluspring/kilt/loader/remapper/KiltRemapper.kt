package xyz.bluspring.kilt.loader.remapper

import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.tinyremapper.TinyRemapper
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.KiltLoader
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipFile

// You might think it's more efficient to just use Tiny Remapper directly,
// but for whatever reason, and I had been debugging this for like 3 hours,
// it wouldn't remap fields and methods. It remapped classes absolutely perfectly, but not those two.
// If you figure out why that is the case, please, be my guest and swap this to just
// entirely use Tiny.
class KiltRemapper(private val remapTree: TinyTree, private val from: String, private val to: String) {
    fun remapJar(file: File): File {
        val hash = DigestUtils.md5Hex(file.inputStream())
        val remappedModFile = File(remappedModsDir, "$hash.jar")

        if (remappedModFile.exists())
            return remappedModFile

        val jar = JarFile(file)
        val remapper = KiltAsmRemapper(remapTree, from, to)
        val entries = mutableMapOf<String, ByteArray>()

        // god forgive me for what atrocities i've just committed in this single method
        jar.entries().iterator().forEach { classEntry ->
            if (!classEntry.name.endsWith(".class")) {
                entries[classEntry.name] = jar.getInputStream(classEntry).readAllBytes()
                return@forEach
            }

            val classReader = ClassReader(jar.getInputStream(classEntry))
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            val remappedClassNode = ClassNode(Opcodes.ASM9)
            val classRemapper = ClassRemapper(remappedClassNode, remapper)
            classNode.accept(classRemapper)

            val classWriter = ClassWriter(0)
            remappedClassNode.accept(classWriter)
            entries[classEntry.name] = classWriter.toByteArray()
        }

        remappedModFile.createNewFile()
        val remappedJar = JarOutputStream(remappedModFile.outputStream())
        entries.forEach { (name, data) ->
            remappedJar.putNextEntry(JarEntry(name))
            remappedJar.write(data)
        }
        remappedJar.close()

        return remappedModFile
    }

    companion object {
        val remappedModsDir = File(KiltLoader.kiltCacheDir, "remappedMods").apply {
            if (!this.exists())
                this.mkdirs()
        }
    }
}