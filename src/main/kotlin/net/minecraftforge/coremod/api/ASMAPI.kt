package net.minecraftforge.coremod.api

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.util.TraceMethodVisitor
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.asm.CoreMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Modifier
import java.util.*
import javax.script.ScriptException
import kotlin.math.max
import kotlin.math.min

// Mostly copied from https://github.com/MinecraftForge/CoreMods/blob/master/src/main/java/net/minecraftforge/coremod/api/ASMAPI.java
object ASMAPI {
    private val logger = LoggerFactory.getLogger("CoreMod ASM API")

    @JvmStatic
    fun getMethodNode(): MethodNode {
        return MethodNode(Opcodes.ASM6)
    }

    @JvmStatic
    fun appendMethodCall(node: MethodNode, call: MethodInsnNode) {
        node.instructions.insertBefore(node.instructions.first, call)
    }

    enum class MethodType {
        VIRTUAL, SPECIAL, STATIC, INTERFACE, DYNAMIC;

        fun toOpcode(): Int {
            return Opcodes.INVOKEVIRTUAL + this.ordinal
        }
    }

    @JvmStatic
    fun buildMethodCall(
        ownerName: String,
        methodName: String,
        methodDescriptor: String,
        type: MethodType
    ): MethodInsnNode {
        return MethodInsnNode(type.toOpcode(), ownerName, methodName, methodDescriptor, type == MethodType.INTERFACE)
    }

    @JvmStatic
    fun mapMethod(name: String): String {
        return KiltRemapper.srgMappedMethods[name]?.second ?: name
    }

    @JvmStatic
    fun mapField(name: String): String {
        return KiltRemapper.srgMappedFields[name]?.second ?: name
    }

    /**
     * Checks if the given JVM property (or if the property prepended with `"coremod."`) is `true`.
     *
     * @param propertyName the property to check
     * @return true if the property is true
     */
    @JvmStatic
    fun getSystemPropertyFlag(propertyName: String): Boolean {
        return java.lang.Boolean.getBoolean(propertyName) || java.lang.Boolean.getBoolean("coremod.$propertyName")
    }

    enum class InsertMode {
        REMOVE_ORIGINAL, INSERT_BEFORE, INSERT_AFTER
    }

    /**
     * Finds the first instruction with matching opcode
     *
     * @param method the method to search in
     * @param opCode the opcode to search for
     * @return the found instruction node or null if none matched
     */
    @JvmStatic
    fun findFirstInstruction(method: MethodNode, opCode: Int): AbstractInsnNode? {
        return findFirstInstructionAfter(method, opCode, 0)
    }

    /**
     * Finds the first instruction with matching opcode after the given start index
     *
     * @param method the method to search in
     * @param opCode the opcode to search for
     * @param startIndex the index to start search after (inclusive)
     * @return the found instruction node or null if none matched after the given index
     */
    @JvmStatic
    fun findFirstInstructionAfter(method: MethodNode, opCode: Int, startIndex: Int): AbstractInsnNode? {
        for (i in max(0, startIndex) until method.instructions.size()) {
            val ain = method.instructions[i]
            if (ain.opcode == opCode) {
                return ain
            }
        }

        return null
    }

    /**
     * Finds the first instruction with matching opcode before the given index in reverse search
     *
     * @param method the method to search in
     * @param opCode the opcode to search for
     * @param startIndex the index at which to start searching (inclusive)
     * @return the found instruction node or null if none matched before the given startIndex
     */
    @JvmStatic
    fun findFirstInstructionBefore(method: MethodNode, opCode: Int, startIndex: Int): AbstractInsnNode? {
        for (i in max((method.instructions.size() - 1), startIndex) downTo 0) {
            val ain = method.instructions[i]
            if (ain.opcode == opCode) {
                return ain
            }
        }
        return null
    }

    /**
     * Finds the first method call in the given method matching the given type, owner, name and descriptor
     *
     * @param method the method to search in
     * @param type the type of method call to search for
     * @param owner the method call's owner to search for
     * @param name the method call's name
     * @param descriptor the method call's descriptor
     * @return the found method call node or null if none matched
     */
    @JvmStatic
    fun findFirstMethodCall(
        method: MethodNode, type: MethodType,
        owner: String, name: String, descriptor: String
    ): MethodInsnNode? {
        return findFirstMethodCallAfter(method, type, owner, name, descriptor, 0)
    }

    /**
     * Finds the first method call in the given method matching the given type, owner, name and descriptor
     * after the instruction given index
     *
     * @param method the method to search in
     * @param type the type of method call to search for
     * @param owner the method call's owner to search for
     * @param name the method call's name
     * @param descriptor the method call's descriptor
     * @param startIndex the index after which to start searching (inclusive)
     * @return the found method call node, null if none matched after the given index
     */
    @JvmStatic
    fun findFirstMethodCallAfter(
        method: MethodNode, type: MethodType,
        owner: String, name: String, descriptor: String,
        startIndex: Int
    ): MethodInsnNode? {
        for (i in max(0, startIndex) until method.instructions.size()) {
            val node = method.instructions[i]

            if (node is MethodInsnNode && node.opcode == type.toOpcode()) {
                if (node.owner == owner && node.name == name && node.desc == descriptor) {
                    return node
                }
            }
        }

        return null
    }

    /**
     * Finds the first method call in the given method matching the given type, owner, name and descriptor
     * before the given index in reverse search
     *
     * @param method the method to search in
     * @param type the type of method call to search for
     * @param owner the method call's owner to search for
     * @param name the method call's name
     * @param descriptor the method call's descriptor
     * @param startIndex the index at which to start searching (inclusive)
     * @return the found method call node or null if none matched before the given startIndex
     */
    @JvmStatic
    fun findFirstMethodCallBefore(
        method: MethodNode, type: MethodType,
        owner: String, name: String, descriptor: String,
        startIndex: Int
    ): MethodInsnNode? {
        for (i in min((method.instructions.size() - 1), startIndex) downTo 0) {
            val node = method.instructions[i]

            if (node is MethodInsnNode && node.opcode == type.toOpcode()) {
                if (node.owner == owner && node.name == name && node.desc == descriptor) {
                    return node
                }
            }
        }

        return null
    }

    /**
     * Inserts/replaces a list after/before first [MethodInsnNode] that matches the parameters of these functions in the method provided.
     * Only the first node matching is targeted, all other matches are ignored.
     * @param method The method where you want to find the node
     * @param type The type of the old method node.
     * @param owner The owner of the old method node.
     * @param name The name of the old method node. You may want to use [.mapMethod] if this is a srg name
     * @param desc The desc of the old method node.
     * @param list The list that should be inserted
     * @param mode How the given code should be inserted
     * @return True if the node was found, false otherwise
     */
    @JvmStatic
    fun insertInsnList(
        method: MethodNode, type: MethodType,
        owner: String, name: String, desc: String,
        list: InsnList?, mode: InsertMode
    ): Boolean {
        val nodeIterator = method.instructions.iterator()
        val opcode = type.toOpcode()

        while (nodeIterator.hasNext()) {
            val next = nodeIterator.next()
            if (next.opcode == opcode && next is MethodInsnNode) {
                if (next.owner == owner && next.name == name && next.desc == desc) {
                    if (mode == InsertMode.INSERT_BEFORE)
                        method.instructions.insertBefore(next, list)
                    else
                        method.instructions.insert(next, list)

                    if (mode == InsertMode.REMOVE_ORIGINAL)
                        nodeIterator.remove()

                    return true
                }
            }
        }

        return false
    }

    /**
     * Builds a new [InsnList] out of the specified AbstractInsnNodes
     * @param nodes The nodes you want to add
     * @return A new list with the nodes
     */
    @JvmStatic
    fun listOf(vararg nodes: AbstractInsnNode?): InsnList {
        val list = InsnList()

        for (node in nodes)
            list.add(node)

        return list
    }

    /**
     * Rewrites accesses to a specific field in the given class to a method-call.
     *
     * The field specified by fieldName must be private and non-static.
     * The method-call the field-access is redirected to does not take any parameters and returns an object of the
     * same type as the field.
     * If no methodName is passed, any method matching the described signature will be used as callable method.
     *
     * @param classNode the class to rewrite the accesses in
     * @param fieldName the field accesses should be redirected to
     * @param methodName the name of the method to redirect accesses through,
     * or null if any method with matching signature should be applicable
     */
    @JvmStatic
    fun redirectFieldToMethod(classNode: ClassNode, fieldName: String, methodName: String?) {
        var foundMethod: MethodNode? = null
        var foundField: FieldNode? = null
        for (fieldNode in classNode.fields) {
            if (Objects.equals(fieldNode.name, fieldName)) {
                if (foundField == null) {
                    foundField = fieldNode
                } else {
                    throw IllegalStateException("Found multiple fields with name $fieldName")
                }
            }
        }

        checkNotNull(foundField) { "No field with name $fieldName found" }
        // Kilt: avoid checking isPrivate
        check(!(/*!Modifier.isPrivate(foundField.access) ||*/ Modifier.isStatic(foundField.access))) { "Field $fieldName is not private and an instance field" }

        val methodSignature = "()" + foundField.desc

        for (methodNode in classNode.methods) {
            if (methodNode.desc == methodSignature) {
                if (foundMethod == null && Objects.equals(methodNode.name, methodName)) {
                    foundMethod = methodNode
                } else if (foundMethod == null && methodName == null) {
                    foundMethod = methodNode
                } else check(
                    !(foundMethod != null && (methodName == null || Objects.equals(
                        methodNode.name,
                        methodName
                    )))
                ) { "Found duplicate method with signature $methodSignature" }
            }
        }

        if (foundMethod == null) {
            logger.error("Unable to find method $methodSignature. Skipping.")
            return
        }

        for (methodNode in classNode.methods) {
            // skip the found getter method
            if (methodNode == foundMethod) continue

            if (methodNode.desc != methodSignature) {
                val iterator: MutableListIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val insnNode = iterator.next()
                    if (insnNode.opcode == Opcodes.GETFIELD) {
                        val fieldInsnNode = insnNode as FieldInsnNode

                        if (Objects.equals(fieldInsnNode.name, fieldName)) {
                            iterator.remove()

                            val replace = MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                classNode.name,
                                foundMethod.name, foundMethod.desc,
                                false
                            )

                            iterator.add(replace)
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    @Throws(ScriptException::class, IOException::class)
    fun loadFile(file: String): Boolean {
        return CoreMod.tracked?.loadAdditionalFile(file) ?: false
    }

    @JvmStatic
    @Throws(ScriptException::class, IOException::class)
    fun loadData(file: String): Any? {
        return CoreMod.tracked?.loadAdditionalData(file)
    }

    @JvmStatic
    fun log(level: String, message: String, vararg args: Any?) {
        CoreMod.tracked?.logMessage(level, message, args)
    }

    @JvmStatic
    fun classNodeToString(node: ClassNode): String {
        val text = Textifier()
        node.accept(TraceClassVisitor(null, text, null))

        return toString(text)
    }

    @JvmStatic
    fun fieldNodeToString(node: FieldNode): String {
        val text = Textifier()
        node.accept(TraceClassVisitor(null, text, null))

        return toString(text)
    }

    @JvmStatic
    fun methodNodeToString(node: MethodNode): String {
        val text = Textifier()
        node.accept(TraceMethodVisitor(text))

        return toString(text)
    }

    @JvmStatic
    private fun toString(text: Textifier): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        text.print(pw)
        pw.flush()

        return sw.toString()
    }
}