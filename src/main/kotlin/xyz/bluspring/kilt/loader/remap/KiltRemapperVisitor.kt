package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.FieldDef
import net.fabricmc.mapping.tree.MethodDef
import net.fabricmc.mapping.tree.TinyTree
import org.objectweb.asm.*
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode

class KiltRemapperVisitor(
    private val srgIntermediaryTree: TinyTree,
    private val kiltWorkaroundTree: TinyTree,
    private val classNode: ClassNode
) {
    private fun getDefsFromField(name: String, descriptor: String): Pair<ClassDef, FieldDef>? {
        val intermediaryClass = srgIntermediaryTree.classes.firstOrNull { it.fields.any { field -> field.getRawName("srg") == name && remapSrgDescriptor(field.getDescriptor("srg")) == descriptor } }
            ?: return null

        val intermediaryField = intermediaryClass.fields.firstOrNull { it.getRawName("srg") == name && remapSrgDescriptor(it.getDescriptor("srg")) == descriptor } ?: return null

        if (!useNamed)
            return Pair(intermediaryClass, intermediaryField)

        val namedClass = mappings.classes.firstOrNull { it.getRawName("intermediary") == intermediaryClass.getRawName("intermediary") } ?: return null
        val namedField = namedClass.fields.firstOrNull { it.getRawName("intermediary") == intermediaryField.getRawName("intermediary") && it.getDescriptor("intermediary") == intermediaryField.getDescriptor("intermediary") } ?: return null

        return Pair(namedClass, namedField)
    }

    private fun getDefsFromMethod(name: String, descriptor: String): Pair<ClassDef, MethodDef>? {
        val intermediaryClass = srgIntermediaryTree.classes.firstOrNull { it.methods.any { method -> method.getRawName("srg") == name && remapSrgDescriptor(method.getDescriptor("srg")) == descriptor } }
            ?: return null

        val intermediaryMethod = intermediaryClass.methods.firstOrNull {
            it.getRawName("srg") == name
                      // I TRUSTED YOU
                    && remapSrgDescriptor(it.getDescriptor("srg")) == descriptor
        } ?: return null

        if (!useNamed)
            return Pair(intermediaryClass, intermediaryMethod)

        val namedClass = mappings.classes.firstOrNull { it.getRawName("intermediary") == intermediaryClass.getRawName("intermediary") } ?: return null
        val namedMethod = namedClass.methods.firstOrNull { it.getRawName("intermediary") == intermediaryMethod.getRawName("intermediary") && it.getDescriptor("intermediary") == intermediaryMethod.getDescriptor("intermediary") } ?: return null

        return Pair(namedClass, namedMethod)
    }

    private fun remapClass(name: String): String {
        if (name.startsWith("net/minecraftforge")) { // use the workarounds instead
            return kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt") ?: name
        }

         val intermediaryName = srgIntermediaryTree.classes.firstOrNull { it.getRawName("srg") == name }?.getRawName("intermediary")
             ?: return name

        if (useNamed)
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

    private fun remapSrgDescriptor(descriptor: String): String {
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

                    val name = incompleteString.removePrefix("L").removeSuffix(";")
                    val intermediaryName = srgIntermediaryTree.classes.firstOrNull { classDef -> classDef.getRawName("intermediary") == name }?.getRawName("srg")

                    formedString += intermediaryName ?: name

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
            val defs = getDefsFromField(field.name, field.desc) ?: return@forEach
            val descriptor = if (field.desc != null) remapDescriptor(field.desc) else null
            val signature = if (field.signature != null) remapSignature(field.signature) else null

            field.name = defs.second.getRawName(namespace)
            field.desc = descriptor
            field.signature = signature
        }

        classNode.methods.forEach { method ->
            val defs = getDefsFromMethod(method.name, method.desc) ?: return@forEach
            val signature = if (method.signature != null) remapSignature(method.signature) else null

            method.name = defs.second.getRawName(namespace)
            method.desc = defs.second.getDescriptor(namespace)
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

                        val methodDefs = getDefsFromMethod(it.name, it.desc)

                        if (methodDefs == null && it.name.endsWith("_") && it.name.startsWith("m_")) {
                            println("${it.owner}#${it.name}${it.desc} under ${classNode.name}#${method.name}${method.desc}")
                            println("Failed to remap ${it.name}!")

                            return@insn
                        } else if (methodDefs == null)
                            return@insn

                        val owner = methodDefs.first.getRawName(namespace)
                        val name = methodDefs.second.getRawName(namespace)
                        val desc = methodDefs.second.getDescriptor(namespace)

                        val methodInsn = MethodInsnNode(it.opcode, owner, name, desc, it.itf)

                        method.instructions.insert(it, methodInsn)
                        method.instructions.remove(it)
                    }

                    Opcodes.INVOKEDYNAMIC -> {
                        if (it !is InvokeDynamicInsnNode)
                            return@insn

                        val methodDefs = getDefsFromMethod(it.name, it.desc) ?: return@insn

                        val name = methodDefs.second.getRawName(namespace)
                        val desc = methodDefs.second.getDescriptor(namespace)

                        val bsmMethodDefs = getDefsFromMethod(it.bsm.name, it.bsm.desc)

                        val handle = if (bsmMethodDefs != null) {
                            val bsmOwner = bsmMethodDefs.first.getRawName(namespace)
                            val bsmName = bsmMethodDefs.second.getRawName(namespace)
                            val bsmDesc = bsmMethodDefs.second.getDescriptor(namespace)

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

                        val desc = remapClass(it.desc)

                        val typeInsn = TypeInsnNode(it.opcode, desc)
                        method.instructions.insert(it, typeInsn)
                        method.instructions.remove(it)
                    }

                    // fields
                    Opcodes.PUTFIELD, Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.GETSTATIC -> {
                        if (it !is FieldInsnNode)
                            return@insn

                        val fieldDefs = getDefsFromField(it.name, it.desc)

                        if (fieldDefs == null && it.name.endsWith("_") && it.name.startsWith("f_")) {
                            println("${it.owner}.${it.name} ${it.desc} under ${classNode.name}#${method.name}${method.desc}")
                            println("Failed to remap ${it.name}!")

                            return@insn
                        } else if (fieldDefs == null)
                            return@insn

                        val owner = fieldDefs.first.getRawName(namespace)
                        val name = fieldDefs.second.getRawName(namespace)
                        val desc = fieldDefs.second.getDescriptor(namespace)

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
        private val useNamed = launcher.targetNamespace != "intermediary"
        private val mappings = launcher.mappingConfiguration.mappings

        private val namespace: String
            get() {
                return if (useNamed)
                    launcher.targetNamespace
                else "intermediary"
            }
    }
}