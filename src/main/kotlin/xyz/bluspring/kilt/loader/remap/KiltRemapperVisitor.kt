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

    fun write() {
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

            val insnList = InsnList()

            method.instructions.forEach insn@{
                when (it.opcode) {
                    // methods
                    Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESTATIC, Opcodes.INVOKESPECIAL,
                    Opcodes.INVOKEINTERFACE -> {
                        val methodInsn = it as MethodInsnNode

                        val methodDefs = getDefsFromMethod(methodInsn.name, methodInsn.desc) ?: return@insn

                        methodInsn.owner = methodDefs.first.getRawName(namespace)
                        methodInsn.name = methodDefs.second.getRawName(namespace)
                        methodInsn.desc = methodDefs.second.getDescriptor(namespace)

                        insnList.add(methodInsn)
                    }

                    Opcodes.INVOKEDYNAMIC -> {
                        val methodInsn = it as InvokeDynamicInsnNode

                        val methodDefs = getDefsFromMethod(methodInsn.name, methodInsn.desc) ?: return@insn

                        methodInsn.name = methodDefs.second.getRawName(namespace)
                        methodInsn.desc = methodDefs.second.getDescriptor(namespace)

                        insnList.add(methodInsn)
                    }

                    // classes
                    Opcodes.CHECKCAST, Opcodes.NEW, Opcodes.ANEWARRAY, Opcodes.INSTANCEOF -> {
                        val typeInsn = it as TypeInsnNode

                        typeInsn.desc = remapClass(typeInsn.desc)

                        insnList.add(typeInsn)
                    }

                    // fields
                    Opcodes.PUTFIELD, Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.GETSTATIC -> {
                        val fieldInsn = it as FieldInsnNode

                        val fieldDefs = getDefsFromField(fieldInsn.name, fieldInsn.desc) ?: return@insn

                        fieldInsn.owner = fieldDefs.first.getRawName(namespace)
                        fieldInsn.name = fieldDefs.second.getRawName(namespace)
                        fieldInsn.desc = fieldDefs.second.getDescriptor(namespace)

                        insnList.add(fieldInsn)
                    }

                    else -> {
                        insnList.add(it)
                    }
                }
            }

            method.instructions = insnList
        }
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