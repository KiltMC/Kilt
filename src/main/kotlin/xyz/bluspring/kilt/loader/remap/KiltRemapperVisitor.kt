package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.mapping.tree.TinyTree
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import org.objectweb.asm.tree.*

class KiltRemapperVisitor(
    private val srgIntermediaryTree: TinyTree,
    private val kiltWorkaroundTree: TinyTree,
    private val classNode: ClassNode,

    private val classMappings: Map<String, String>,
    private val fieldMappings: Map<String, Pair<String, String>>,
    private val methodMappings: Map<Pair<String, String>, Pair<String, String>>
) {
    private fun remapClass(name: String): String {
        val workaround = kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt")

        if (workaround != null || name.startsWith("net/minecraftforge")) { // use the workarounds instead
            return workaround ?: name
        }

         val intermediaryName = srgIntermediaryTree.classes.firstOrNull { it.getRawName("searge") == name }?.getRawName("intermediary")
             ?: return name

        if (KiltRemapper.useNamed)
            return mappings.classes.firstOrNull { it.getRawName("intermediary") == intermediaryName }?.getRawName("named") ?: name

        return intermediaryName
    }

    private fun remapDescriptor(descriptor: String): String {
        var formedString = ""

        var incompleteString = ""
        var isInClass = false
        descriptor.forEach {
            if (it == 'L' && !isInClass)
                isInClass = true

            if (isInClass) {
                incompleteString += it

                if (it == ';') {
                    isInClass = false

                    formedString += 'L'
                    formedString += remapClass(incompleteString.removePrefix("L").removeSuffix(";"))
                    formedString += ';'

                    incompleteString = ""
                }
            } else {
                formedString += it
            }
        }

        return formedString
    }

    private fun remapSignature(signature: String): String {
        val parser = SignatureReader(signature)
        val writer = object : SignatureWriter() {
            override fun visitClassType(name: String?) {
                super.visitClassType(if (name != null) remapClass(name) else null)
            }

            override fun visitInnerClassType(name: String?) {
                super.visitInnerClassType(if (name != null) remapClass(name) else null)
            }

            override fun visitTypeVariable(name: String?) {
                super.visitTypeVariable(if (name != null) remapClass(name) else null)
            }
        }

        parser.accept(writer)

        return writer.toString()
    }

    fun write(): ClassNode {
        classNode.superName = if (classNode.superName != null)
            remapClass(classNode.superName)
        else null

        val newInterfaces = mutableListOf<String>()
        classNode.interfaces?.forEach {
            newInterfaces.add(remapClass(it))
        }

        classNode.signature = if (classNode.signature == null) null else remapSignature(classNode.signature)
        classNode.interfaces = newInterfaces

        classNode.fields.forEach { field ->
            val mapping = fieldMappings[field.name] ?: return@forEach
            val signature = if (field.signature != null) remapSignature(field.signature) else null

            field.name = mapping.first
            field.desc = mapping.second
            field.signature = signature
        }

        classNode.methods.forEach { method ->
            val mapping = methodMappings[Pair(method.name, method.desc)] ?: return@forEach
            val signature = if (method.signature != null) remapSignature(method.signature) else null

            method.name = mapping.first
            method.desc = mapping.second
            method.signature = signature

            method.localVariables.forEach local@{
                it.desc = remapDescriptor(it.desc)
                it.signature = if (it.signature == null) null else remapSignature(it.signature)
            }

            method.instructions.forEach insn@{
                when (it.opcode) {
                    // methods
                    Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESTATIC, Opcodes.INVOKESPECIAL,
                    Opcodes.INVOKEINTERFACE -> {
                        if (it !is MethodInsnNode)
                            return@insn

                        val methodMapping = methodMappings[Pair(it.name, it.desc)]

                        if (methodMapping == null && it.name.endsWith("_") && it.name.startsWith("m_")) {
                            println("${it.owner}#${it.name}${it.desc} under ${classNode.name}#${method.name}${method.desc}")
                            println("Failed to remap ${it.name}!")

                            return@insn
                        } else if (methodMapping == null)
                            return@insn

                        val owner = classMappings[it.owner] ?: it.owner
                        val name = methodMapping.first
                        val desc = methodMapping.second

                        val methodInsn = MethodInsnNode(it.opcode, owner, name, desc, it.itf)

                        method.instructions.insert(it, methodInsn)
                        method.instructions.remove(it)
                    }

                    Opcodes.INVOKEDYNAMIC -> {
                        if (it !is InvokeDynamicInsnNode)
                            return@insn

                        val methodMapping = methodMappings[Pair(it.name, it.desc)]

                        val name = methodMapping?.first ?: it.name
                        val desc = methodMapping?.second ?: it.desc

                        val bsmMethodMapping = methodMappings[Pair(it.bsm.name, it.bsm.desc)]

                        val handle = if (bsmMethodMapping != null) {
                            val bsmOwner = classMappings[it.bsm.owner] ?: it.bsm.owner
                            val bsmName = bsmMethodMapping.first
                            val bsmDesc = bsmMethodMapping.second

                            Handle(it.bsm.tag, bsmOwner, bsmName, bsmDesc, it.bsm.isInterface)
                        } else it.bsm

                        val methodInsn = InvokeDynamicInsnNode(name, desc, handle, it.bsmArgs)

                        method.instructions.insert(it, methodInsn)
                        method.instructions.remove(it)
                    }

                    // classes
                    Opcodes.CHECKCAST, Opcodes.NEW, Opcodes.ANEWARRAY, Opcodes.INSTANCEOF -> {
                        if (it !is TypeInsnNode)
                            return@insn

                        val desc = classMappings[it.desc] ?: it.desc

                        val typeInsn = TypeInsnNode(it.opcode, desc)
                        method.instructions.insert(it, typeInsn)
                        method.instructions.remove(it)
                    }

                    // fields
                    Opcodes.PUTFIELD, Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.GETSTATIC -> {
                        if (it !is FieldInsnNode)
                            return@insn

                        val fieldDefs = fieldMappings[it.name]

                        if (fieldDefs == null && it.name.endsWith("_") && it.name.startsWith("f_")) {
                            println("${it.owner}.${it.name} ${it.desc} under ${classNode.name}#${method.name}${method.desc}")
                            println("Failed to remap ${it.name}!")

                            return@insn
                        } else if (fieldDefs == null)
                            return@insn

                        val owner = classMappings[it.owner] ?: it.owner
                        val name = fieldDefs.first
                        val desc = fieldDefs.second

                        val fieldInsn = FieldInsnNode(it.opcode, owner, name, desc)

                        method.instructions.insert(it, fieldInsn)
                        method.instructions.remove(it)
                    }
                }
            }
        }

        return classNode
    }

    companion object {
        private val launcher = FabricLauncherBase.getLauncher()
        private val mappings = launcher.mappingConfiguration.mappings
    }
}