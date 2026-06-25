package xyz.bluspring.kilt.workarounds

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import xyz.bluspring.fork.mm.api.ClassTinkerers

object ModifiedCloneWorkaroundLoader {

    val REMAPPED_TYPES = mutableMapOf<String, String>()
    val TRANSFORMERS = mutableListOf<Runnable>()

    init {
        val monsterType = ("net/minecraft/world/entity/monster/Monster")
        val monsterDesc = "L$monsterType;"
        val mobType = ("net/minecraft/world/entity/Mob")
        val mobDesc = "L$mobType;"
        val rangedAttackMob = "Lnet/minecraft/world/entity/monster/RangedAttackMob;"
        run {
            val bowAttackGoal = "net/minecraft/world/entity/ai/goal/RangedBowAttackGoal"
            val bowAttackGoalClone = "xyz/bluspring/kilt/workarounds/RangedBowAttackGoalWorkaround"
            addTransformedClone(
                (bowAttackGoal),
                bowAttackGoalClone,
            ) { classNode ->
                replaceType(classNode, monsterType, mobType)

                val fallbackConstructor = MethodNode(
					Opcodes.ACC_PUBLIC, "<init>", "(${monsterDesc}DIF)V",
					"<M:$monsterDesc:$rangedAttackMob>(TM;DIF)V", null
				).apply {
                    val startLabel = LabelNode()
                    this.instructions.add(startLabel)
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 1))
                    this.instructions.add(VarInsnNode(Opcodes.DLOAD, 2)) // Fun fact, double parameters take 2 variable slots.
                    this.instructions.add(VarInsnNode(Opcodes.ILOAD, 4))
                    this.instructions.add(VarInsnNode(Opcodes.FLOAD, 5))
                    this.instructions.add(MethodInsnNode(
                        Opcodes.INVOKESPECIAL, bowAttackGoalClone,
                        "<init>", "(${mobDesc}DIF)V"
                    ))
                    this.instructions.add(LabelNode())
                    this.instructions.add(InsnNode(Opcodes.RETURN))
                    val endLabel = LabelNode()
                    this.instructions.add(endLabel)

                    this.localVariables.add(LocalVariableNode("this", "L$bowAttackGoalClone;", "L$bowAttackGoalClone<TT;>;", startLabel, endLabel, 0))
                    this.localVariables.add(LocalVariableNode("mob", monsterDesc, "TM;", startLabel, endLabel, 1))
                    this.localVariables.add(LocalVariableNode("speedModifier", "D", null, startLabel, endLabel, 2))
                    this.localVariables.add(LocalVariableNode("attackIntervalMin", "I", null, startLabel, endLabel, 4))
                    this.localVariables.add(LocalVariableNode("attackRadius", "F", null, startLabel, endLabel, 5))
                }
                classNode.methods.add(fallbackConstructor)
            }
        }

        run {
            val crossbowAttackGoal = "net/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal"
            val crossbowAttackGoalClone = "xyz/bluspring/kilt/workarounds/RangedCrossbowAttackGoalWorkaround"
            addTransformedClone(
                (crossbowAttackGoal),
                crossbowAttackGoalClone,
            ) { classNode ->
                replaceType(classNode, monsterType, mobType)

                val fallbackConstructor = MethodNode(
					Opcodes.ACC_PUBLIC,
	                "<init>",
	                "(${monsterDesc}DF)V",
					"<M:$monsterDesc:$rangedAttackMob:Lnet/minecraft/world/entity/monster/CrossbowAttackMob;>(TM;DF)V",
	                null
				).apply {
                    val startLabel = LabelNode()
                    this.instructions.add(startLabel)
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 1))
                    this.instructions.add(VarInsnNode(Opcodes.DLOAD, 2)) // Fun fact, double parameters take 2 variable slots.
                    this.instructions.add(VarInsnNode(Opcodes.FLOAD, 4))
                    this.instructions.add(MethodInsnNode(
                        Opcodes.INVOKESPECIAL, crossbowAttackGoalClone,
                        "<init>", "(${mobDesc}DF)V"
                    ))
                    this.instructions.add(LabelNode())
                    this.instructions.add(InsnNode(Opcodes.RETURN))
                    val endLabel = LabelNode()
                    this.instructions.add(endLabel)

                    this.localVariables.add(LocalVariableNode("this", "L$crossbowAttackGoalClone;", "L$crossbowAttackGoalClone<TT;>;", startLabel, endLabel, 0))
                    this.localVariables.add(LocalVariableNode("mob", monsterDesc, "TM;", startLabel, endLabel, 1))
                    this.localVariables.add(LocalVariableNode("speedModifier", "D", null, startLabel, endLabel, 2))
                    this.localVariables.add(LocalVariableNode("attackRadius", "F", null, startLabel, endLabel, 4))
                }
                classNode.methods.add(fallbackConstructor)
            }
        }
    }

    fun load() {
        TRANSFORMERS.forEach(Runnable::run)
        TRANSFORMERS.clear()
    }

    private fun mapType(type: Type, oldTypeName: String, newTypeDesc: String): Type {
        when (type.sort) {
            Type.OBJECT -> {
                if (type.internalName == oldTypeName) {
                    return Type.getType(newTypeDesc)
                }
            }
            Type.ARRAY -> {
                val dimensions = type.dimensions
                val elementType = type.elementType
                if (elementType.internalName == oldTypeName) {
                    return Type.getType("[".repeat(dimensions) + newTypeDesc)
                }
            }
        }
        return type
    }

    private fun mapHandle(handle: Handle, oldTypeName: String, newTypeName: String): Handle {
        if (handle.owner == oldTypeName) {
            return Handle(handle.tag, newTypeName, handle.name, handle.desc, handle.isInterface)
        }
        return handle
    }

    private fun mapFrameTypes(frameTypes: List<Any?>?, oldOwnerName: String, newOwnerName: String): List<Any?>? {
        if (frameTypes == null) return frameTypes
        var newTypes: MutableList<Any?>? = null
        for (i in frameTypes.indices) {
            val type = frameTypes[i]
            if (type is String && type == oldOwnerName) {
                if (newTypes == null) {
                    newTypes = frameTypes.toMutableList()
                }
                newTypes[i] = newOwnerName
            }
        }
        return newTypes ?: frameTypes
    }

    private fun setClassName(node: ClassNode, oldOwnerName: String, newOwnerName: String) {
        node.name = newOwnerName
        replaceType(node, oldOwnerName, newOwnerName)
    }

    fun addTransformedClone(
        targetClass: String, cloneClass: String,
        transformer: (ClassNode) -> Unit
    ) {
        REMAPPED_TYPES[targetClass] = cloneClass
        TRANSFORMERS.add {
            var originalNode: ClassNode? = null
            ClassTinkerers.addPostTransformation(targetClass) { classNode ->
                originalNode = classNode
            }

            // Create an empty class so we don't get ClassNotFoundException when classloading.
            // Only the class name actually matters, they will all be overwritten during classloading.
            // 52 is the class version used by Java 8: https://docs.oracle.com/javase/specs/jvms/se26/html/jvms-1.html#jvms-1.2-220
            val cw = ClassWriter(0)
            cw.visit(52, Opcodes.ACC_PUBLIC, cloneClass, null, "java/lang/Object", null)
            ClassTinkerers.define(cloneClass, cw.toByteArray())

            ClassTinkerers.addPostTransformation(cloneClass) { classNode ->
                Class.forName(targetClass.replace("/", ".")) // Classload it somehow so the transform always runs first
                originalNode?.accept(classNode)

                setClassName(classNode, targetClass, cloneClass)

                transformer(classNode)
            }
        }
    }

    fun replaceType(
		classNode: ClassNode,
	    oldType: String,
	    newType: String
    ) {
        val oldDescriptor = "L$oldType;"
        val newDescriptor = "L$newType;"
        classNode.signature = classNode.signature?.replace(oldDescriptor, newDescriptor)
        if (classNode.superName == oldType) {
            classNode.superName = newType
        }

        classNode.interfaces = classNode.interfaces.map {
            if (it == oldType) {
                return@map newType
            }
            return@map it
        }

        for (field in classNode.fields) {
            if (field.desc == oldDescriptor) {
                field.desc = newDescriptor
            }
            field.signature = field.signature?.replace(oldDescriptor, newDescriptor)
        }

        for (method in classNode.methods) {
            method.desc = method.desc.replace(oldDescriptor, newDescriptor)
            method.signature = method.signature?.replace(oldDescriptor, newDescriptor)
        }

        for (method in classNode.methods) {
            for (insn in method.instructions) {
                when (insn) {
                    is FieldInsnNode -> {
                        if (insn.owner == oldType) {
                            insn.owner = newType
                        }
                        insn.desc = insn.desc.replace(oldDescriptor, newDescriptor)
                    }
                    is MethodInsnNode -> {
                        if (insn.owner == oldType) {
                            insn.owner = newType
                        }
                        insn.desc = insn.desc.replace(oldDescriptor, newDescriptor)
                    }
                    is LdcInsnNode -> {
                        val type = insn.cst
                        if (type is Type) {
                            insn.cst = mapType(type, oldType, newDescriptor)
                        }
                    }
                    is TypeInsnNode -> if (insn.desc == oldType) insn.desc = newType
                    is InvokeDynamicInsnNode -> {
                        for (i in insn.bsmArgs.indices) {
                            val arg = insn.bsmArgs[i]
                            when (arg) {
                                is Type -> insn.bsmArgs[i] = mapType(arg, oldType, newDescriptor)
                                is Handle -> insn.bsmArgs[i] = mapHandle(arg, oldType, newType)
                            }
                        }
                    }
                    is FrameNode -> {
                        insn.local = mapFrameTypes(insn.local, oldType, newDescriptor)
                        insn.stack = mapFrameTypes(insn.stack, oldType, newDescriptor)
                    }
                }
            }
            if (method.localVariables != null) {
                for (local in method.localVariables) {
                    if (local.desc == oldDescriptor) {
                        local.desc = newDescriptor
                    }
                    local.signature = local.signature?.replace(oldType, newType)
                }
            }
            method.signature = method.signature?.replace(oldType, newType)
        }

    }

    fun fixClass(classNode: ClassNode) {
        for ((from, to) in REMAPPED_TYPES) {
            replaceType(classNode, from, to)
        }
    }
}
