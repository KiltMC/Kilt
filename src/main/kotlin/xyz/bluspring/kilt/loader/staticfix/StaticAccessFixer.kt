package xyz.bluspring.kilt.loader.staticfix

import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.superfix.CommonSuperClassWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Pattern

object StaticAccessFixer {
    private val logger = Kilt.logger
    private val modifyPackages = listOf(
        "net/minecraftforge/registries/",
        "xyz/bluspring/kilt/remaps/",
        "xyz/bluspring/kilt/workarounds/",
    )

    private val staticMappings = StaticRemapper.read(Kilt::class.java.getResource("/kilt_static_workaround_mappings.txt")!!.readText())

    fun fixMods(mods: Collection<ForgeMod>, dir: File) {
        logger.info("Modifying mods to workaround a classloading issue...")

        mods.forEach { mod ->
            if (mod.modFile == null)
                return@forEach

            logger.info("Modifying ${mod.modInfo.mod.displayName}...")

            val hash = DigestUtils.md5Hex(mod.remappedModFile.inputStream())
            val modifiedJarFile = File(dir, "${mod.modInfo.mod.modId}_modified_$hash.jar")

            if (modifiedJarFile.exists()) {
                mod.remappedModFile = modifiedJarFile
                return@forEach
            }

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

                try {
                    val classNode = ClassNode(Opcodes.ASM9)
                    val classReader = ClassReader(mod.jar.getInputStream(entry))

                    classReader.accept(classNode, 0)

                    modifyClass(classNode)

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
                } catch (e: Exception) {
                    logger.warn("An exception occurred whilst modifying ${entry.name} in ${mod.modInfo.mod.displayName}, but it should be fine to ignore.")
                    e.printStackTrace()

                    // Don't bother with it, let's just throw it back in and move on.
                    jarOutput.putNextEntry(entry)
                    jarOutput.write(mod.jar.getInputStream(entry).readAllBytes())
                    jarOutput.closeEntry()
                }
            }

            jarOutput.close()
            mod.remappedModFile = modifiedJarFile

            logger.info("Successfully modified ${mod.modInfo.mod.displayName} (${modifiedJarFile.name})!")
        }
    }

    private fun modifyClass(classNode: ClassNode) {
        classNode.methods.forEach { method ->
            val instructionList = mutableListOf<AbstractInsnNode>()
            instructionList.addAll(method.instructions)

            instructionList.forEach instruction@{ instruction ->
                if (instruction.opcode == Opcodes.GETSTATIC) {
                    val fieldInstruction = instruction as FieldInsnNode

                    if (modifyPackages.none { fieldInstruction.owner.startsWith(it) })
                        return@instruction

                    fieldInstruction.owner = staticMappings.tryRemapOwner(fieldInstruction.owner, fieldInstruction.name, fieldInstruction.desc)

                    val insnList = InsnList()

                    // This is equivalent to:
                    // (DESCRIPTOR) Class.forName("OWNER").getDeclaredField("NAME").get(null)
                    insnList.add(LdcInsnNode(fieldInstruction.owner.replace("/", ".")))
                    insnList.add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"))
                    insnList.add(LdcInsnNode(fieldInstruction.name))
                    insnList.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;"))
                    insnList.add(InsnNode(Opcodes.ACONST_NULL))
                    insnList.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;"))
                    insnList.add(TypeInsnNode(Opcodes.CHECKCAST, fieldInstruction.desc.run {
                        if (this.startsWith("L") && this.endsWith(";"))
                            this.trimStart('L').trimEnd(';')
                        else this
                    }))

                    // Place it in front of the original instruction,
                    // only then remove it
                    method.instructions.insert(instruction, insnList)
                    method.instructions.remove(instruction)
                } else if (instruction.opcode == Opcodes.INVOKESTATIC) {
                    val methodInstruction = instruction as MethodInsnNode

                    val remapped = staticMappings.tryRemapOwner(methodInstruction.owner, methodInstruction.name, methodInstruction.desc)
                    if (remapped != methodInstruction.owner)
                        methodInstruction.itf = true
                    methodInstruction.owner = remapped
                }
            }

            method.visitMaxs(0, 0)
        }
    }
}