package xyz.bluspring.kilt.loader.superfix

import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import java.io.File
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

object CommonSuperFixer {
    private val logger = Kilt.logger

    fun fixMods(mods: Collection<ForgeMod>, dir: File) {
        logger.info("Modifying Forge mods to have a common super class...")

        mods.forEach { mod ->
            if (mod.modFile == null)
                return@forEach

            logger.info("Modifying ${mod.modInfo.mod.displayName}...")
            val hash = DigestUtils.md5Hex(mod.remappedModFile.inputStream())
            val modifiedJarFile = File(dir, "${mod.modInfo.mod.modId}_superfix_$hash.jar")
            modifiedJarFile.createNewFile()

            val output = modifiedJarFile.outputStream()
            val jarOutput = JarOutputStream(output)

            for (entry in mod.jar.entries()) {
                if (!entry.name.endsWith(".class")) {
                    jarOutput.putNextEntry(entry)
                    jarOutput.write(mod.jar.getInputStream(entry).readAllBytes())
                    jarOutput.closeEntry()
                    continue
                }

                val classNode = ClassNode(Opcodes.ASM9)
                val classReader = ClassReader(mod.jar.getInputStream(entry))

                classReader.accept(classNode, 0)

                // Let's make sure it doesn't already exist
                if (
                    classNode.access and Opcodes.ACC_INTERFACE == 0
                    && classNode.methods.none { it.name == "<init>" && (it.signature == "()V" || it.desc == "()V") }
                    && classNode.name.endsWith("Event") // this is stupid, but it'll work.
                ) {
                    val method = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", "()V", null)
                    method.visitCode()
                    method.visitVarInsn(Opcodes.ALOAD, 0)
                    method.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false)

                    // init everything with null
                    classNode.fields.forEach { field ->
                        if (field.access and Opcodes.ACC_FINAL != 0) {
                            method.visitVarInsn(Opcodes.ALOAD, 0)
                            method.visitInsn(Opcodes.ACONST_NULL)
                            method.visitFieldInsn(Opcodes.PUTFIELD, classNode.name, field.name, field.desc)
                        }
                    }

                    method.visitInsn(Opcodes.RETURN)
                    method.visitMaxs(0, 0)
                    method.visitEnd()
                }

                val classWriter = CommonSuperClassWriter.createClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classNode, Function {
                    val classEntry = mod.jar.getJarEntry("${it.replace(".", "/")}.class")
                    return@Function if (classEntry == null)
                        null
                    else
                        mod.jar.getInputStream(classEntry).readAllBytes()
                })
                classNode.accept(classWriter)

                jarOutput.putNextEntry(JarEntry(entry.name))
                jarOutput.write(classWriter.toByteArray())
                jarOutput.closeEntry()
            }

            jarOutput.close()
            mod.remappedModFile = modifiedJarFile

            logger.info("Successfully modified ${mod.modInfo.mod.displayName} (${modifiedJarFile.name})!")
        }
    }
}