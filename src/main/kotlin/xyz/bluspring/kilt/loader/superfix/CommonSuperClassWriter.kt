package xyz.bluspring.kilt.loader.superfix

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

// Most of the code here is derived from ModLauncher, literally the only reason why
// this class is needed.
// https://github.com/McModLauncher/modlauncher/blob/main/src/main/java/cpw/mods/modlauncher/TransformerClassWriter.java
class CommonSuperClassWriter(
    writerFlags: Int,
    private val classAccessor: ClassNode,
    private val superClassProvider: Function<String, ByteArray?>
) : ClassWriter(writerFlags) {
    private var computedThis = false

    override fun getCommonSuperClass(type1: String, type2: String): String {
        if (!computedThis) {
            computeHierarchy(classAccessor)
            computedThis = true
        }

        if (getSupers(type2)?.contains(type1) == true)
            return type1

        if (getSupers(type1)?.contains(type2) == true)
            return type2

        if (isIntf(type1) || isIntf(type2))
            return "java/lang/Object"

        var type: String = type1
        do {
            type = getSuper(type) ?: "java/lang/Object"
        } while (getSupers(type2)?.contains(type) == false)

        return type
    }

    private fun getSupers(typeName: String): Set<String>? {
        computeHierarchy(typeName)
        return CLASS_HIERARCHIES[typeName]
    }

    private fun isIntf(typeName: String): Boolean {
        return IS_INTERFACE[typeName] ?: false
    }

    private fun getSuper(typeName: String): String? {
        computeHierarchy(typeName)
        return CLASS_PARENTS[typeName]
    }

    private fun computeHierarchy(classNode: ClassNode) {
        if (!CLASS_HIERARCHIES.contains(classNode.name)) {
            classNode.accept(SuperCollectingVisitor())
        }
    }

    private fun computeHierarchy(className: String) {
        if (CLASS_HIERARCHIES.contains(className))
            return

        val superClass = superClassProvider.apply(className)
        if (superClass == null) {
            try {
                val clazz = Class.forName(className.replace("/", "."))
                computeHierarchyFromClass(className, clazz)
            } catch (_: Exception) {
                CLASS_HIERARCHIES[className] = setOf("java/lang/Object")
            }
        } else {
            computeHierarchyFromFile(className, superClass)
        }
    }

    private fun computeHierarchyFromClass(name: String, clazz: Class<*>) {
        val superClass = clazz.superclass
        val hierarchies = mutableSetOf<String>()

        if (superClass != null) {
            val superName = superClass.name.replace(".", "/")
            CLASS_PARENTS[name] = superName

            if (!CLASS_HIERARCHIES.contains(superName))
                computeHierarchyFromClass(superName, superClass)

            hierarchies.add(name)
            hierarchies.addAll(CLASS_HIERARCHIES[superName] ?: setOf("java/lang/Object"))
        } else {
            hierarchies.add("java/lang/Object")
        }

        IS_INTERFACE[name] = clazz.isInterface
        clazz.interfaces.forEach { interfaceClass ->
            val n = interfaceClass.name.replace(".", "/")
            if (!CLASS_HIERARCHIES.contains(n))
                computeHierarchyFromClass(n, interfaceClass)

            hierarchies.add(n)
            hierarchies.addAll(CLASS_HIERARCHIES[n] ?: setOf("java/lang/Object"))
        }

        CLASS_HIERARCHIES[name] = hierarchies
    }

    private fun computeHierarchyFromFile(name: String, byteArray: ByteArray) {
        val classReader = ClassReader(byteArray)
        classReader.accept(SuperCollectingVisitor(), ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
    }

    companion object {
        private val CLASS_PARENTS = ConcurrentHashMap<String, String>()
        private val CLASS_HIERARCHIES = ConcurrentHashMap<String, Set<String>>()
        private val IS_INTERFACE = ConcurrentHashMap<String, Boolean>()

        fun createClassWriter(mlFlags: Int, classAccessor: ClassNode, superClassProvider: Function<String, ByteArray?>): ClassWriter {
            return CommonSuperClassWriter(mlFlags, classAccessor, superClassProvider)
        }
    }

    inner class SuperCollectingVisitor : ClassVisitor(Opcodes.ASM9) {
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>
        ) {
            val hierarchies = mutableSetOf<String>()

            if (superName != null) {
                CLASS_PARENTS[name] = superName
                computeHierarchy(superName)
                hierarchies.add(name)
                hierarchies.addAll(CLASS_HIERARCHIES[superName] ?: setOf())
            } else {
                hierarchies.add("java/lang/Object")
            }

            IS_INTERFACE[name] = (access and Opcodes.ACC_INTERFACE) != 0
            interfaces.forEach {
                computeHierarchy(it)
                hierarchies.add(it)
                hierarchies.addAll(CLASS_HIERARCHIES[it] ?: setOf())
            }

            CLASS_HIERARCHIES[name] = hierarchies
        }
    }
}