package xyz.bluspring.kilt.loader.remap.fixers

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.lang.reflect.Modifier

// does what ModLauncher does, but ahead of time.
object EventEmptyInitializerFixer {
    fun fixClass(classNode: ClassNode, classList: List<ClassNode>) {
        // let's just run a fixer for everything. i can't be bothered at this point.
        if (Modifier.isInterface(classNode.access) || classNode.access and Opcodes.ACC_ENUM != 0)
            return

        // let's just kindly ignore these..
        if (classNode.name.replace(Regex("[0-9]"), "").endsWith("$") && classNode.outerClass != null)
            return

        // check if the class isn't static and is an inner class
        // also ensure it's not a record
        val isStatic = !(!Modifier.isStatic(classNode.access) && classNode.outerClass != null) || classNode.superName == "java/lang/Record"

        if (classNode.methods.any { m -> m.name == "<init>" && m.desc == "()V" })
            return

        if (classNode.methods.any { m -> m.name == "<init>" && m.desc == "(L${classNode.outerClass};)V" })
            return

        val isEventSubscriber = classNode.methods.any {
            it.visibleAnnotations != null && it.visibleAnnotations.any { a ->
                a.desc == "net/minecraftforge/eventbus/api/SubscribeEvent"
            }
        }

        // ignore non-events
        val classParentHierarchy = recursiveLookupParents(classNode, classList)
        if (!isEventSubscriber && classParentHierarchy.none {
                (it is ClassNode && (it.name == "net/minecraftforge/eventbus/api/Event" || it.name == "net/minecraftforge/fml/event/IModBusEvent")) ||
                        (it is String && (it == "net/minecraftforge/eventbus/api/Event" || it == "net/minecraftforge/fml/event/IModBusEvent"))
        } && classParentHierarchy.none {it is ClassNode &&
            it.methods.any { m ->
                m.visibleAnnotations != null && m.visibleAnnotations.any { a ->
                    a.desc == "net/minecraftforge/eventbus/api/SubscribeEvent"
                }
            }
        })
            return

        // Manually calculate the stack size, as otherwise the ClassWriter has a stroke.
        var stackSize = 1
        val initMethod = classNode.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_SYNTHETIC, "<init>", if (!isStatic) "(L${classNode.outerClass};)V" else "()V", null, null)
        val shouldFirstInit = isEventSubscriber && classNode.methods.none { it.instructions.any { i -> i.opcode == Opcodes.INVOKESPECIAL && i is MethodInsnNode && i.name == "<init>" && i.owner != classNode.name && (i.owner == "java/lang/Object" || i.owner == "net/minecraftforge/eventbus/api/Event") } }
        val firstInitMethod = classNode.methods.filter { it.name == "<init>" }.maxByOrNull { Type.getMethodType(it.desc).argumentTypes.size }

        initMethod.visitCode()

        val label0 = Label()
        initMethod.visitLabel(label0)

        if (!isStatic) {
            // There exists an inner "this" that is invisible at compile-time, and is initialized
            // using the params provided by the initializer.
            initMethod.visitVarInsn(Opcodes.ALOAD, 0)
            initMethod.visitVarInsn(Opcodes.ALOAD, 1)
            initMethod.visitFieldInsn(Opcodes.PUTFIELD, classNode.name, "this$0", "L${classNode.outerClass};")
        }

        initMethod.visitVarInsn(Opcodes.ALOAD, 0)
        if (firstInitMethod != null && shouldFirstInit) { // init this itself
            val methodType = Type.getMethodType(firstInitMethod.desc)
            methodType.argumentTypes.forEach { arg ->
                when (arg.descriptor) {
                    "I" -> {
                        initMethod.visitInsn(Opcodes.ICONST_0)
                    } // int
                    "F" -> initMethod.visitInsn(Opcodes.FCONST_0) // float
                    "D" -> initMethod.visitInsn(Opcodes.DCONST_0) // double
                    "J" -> initMethod.visitInsn(Opcodes.LCONST_0) // long
                    "Z" -> initMethod.visitInsn(Opcodes.ICONST_0) // boolean
                    "S" -> initMethod.visitInsn(Opcodes.ICONST_0) // short
                    "B" -> initMethod.visitInsn(Opcodes.ICONST_0) // byte

                    else -> initMethod.visitInsn(Opcodes.ACONST_NULL)
                }

                stackSize += when (arg.descriptor) {
                    "D", "J" -> 2 // doubles and longs are, of course, 64-bit.

                    else -> 1
                }
            }

            initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.name, "<init>", firstInitMethod.desc, false)
        } else {
            initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false)

            val label = Label()
            initMethod.visitLabel(label)

            val finalFields = classNode.fields.filter { it.access and Opcodes.ACC_FINAL == Opcodes.ACC_FINAL && it.access and Opcodes.ACC_STATIC != Opcodes.ACC_STATIC }

            for (field in finalFields) {
                val l = Label()
                initMethod.visitLabel(l)

                initMethod.visitVarInsn(Opcodes.ALOAD, 0)

                when (field.desc) {
                    "I" -> {
                        initMethod.visitInsn(Opcodes.ICONST_0)
                    } // int
                    "F" -> initMethod.visitInsn(Opcodes.FCONST_0) // float
                    "D" -> initMethod.visitInsn(Opcodes.DCONST_0) // double
                    "J" -> initMethod.visitInsn(Opcodes.LCONST_0) // long
                    "Z" -> initMethod.visitInsn(Opcodes.ICONST_0) // boolean
                    "S" -> initMethod.visitInsn(Opcodes.ICONST_0) // short
                    "B" -> initMethod.visitInsn(Opcodes.ICONST_0) // byte

                    else -> initMethod.visitInsn(Opcodes.ACONST_NULL)
                }

                initMethod.visitFieldInsn(Opcodes.PUTFIELD, classNode.name, field.name, field.desc)

                stackSize += when (field.desc) {
                    "D", "J" -> 2 // doubles and longs are, of course, 64-bit.

                    else -> 1
                }
            }
        }

        initMethod.visitInsn(Opcodes.RETURN)

        val label1 = Label()
        initMethod.visitLabel(label1)

        initMethod.visitLocalVariable("this", "L${classNode.name};", null, label0, label1, 0)
        if (!isStatic) {
            initMethod.visitLocalVariable("this$0", "L${classNode.outerClass};", null, label0, label1, 1)
            initMethod.visitMaxs(stackSize, 2)
        } else {
            initMethod.visitMaxs(stackSize, 1)
        }
        initMethod.visitEnd()
    }

    // speed up the process.
    // if this breaks something, i swear to god...
    private val blacklistedPackageNames = listOf(
        "net/minecraft/",
        "com/mojang/",
        "it/unimi/",
        "java/",
        "kotlin/",
        "kotlinx/",
        "net/fabricmc/",
        "xyz/bluspring/kilt/",
        "org/lwjgl/",
        "sun/",
        "org/apache/",
        "org/jetbrains/",
        "org/ow2/",
        "org/slf4j/",
        "net/java/",
        "io/netty/",
        "com/google/"
    )

    private val classListParentCache = Object2ObjectAVLTreeMap<String, ClassNode>()

    private fun recursiveLookupParents(classNode: ClassNode, classList: List<ClassNode>): List<Any> {
        val list = mutableListOf<Any>()

        if (blacklistedPackageNames.any { classNode.superName.startsWith(it) })
            return list

        val parentClass = classListParentCache.computeIfAbsent(classNode.superName) { name: String ->
            classList.firstOrNull { it.name == name }
        }

        if (parentClass == null) {
            list.add(classNode.superName)
            return list
        }

        list.addAll(recursiveLookupParents(parentClass, classList))

        return list
    }
}