package xyz.bluspring.kilt.util

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import xyz.bluspring.fork.mm.api.ClassTinkerers
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object ModifiedCloneWorkaroundLoader {

    fun init() {
        val monsterType = KiltRemapper.remapClass("net/minecraft/world/entity/monster/Monster")
        val monsterDesc = "L$monsterType;"
        val mobType = KiltRemapper.remapClass("net/minecraft/world/entity/Mob")
        val mobDesc = "L$mobType;"
        val rangedAttackMob = KiltRemapper.remapDescriptor("Lnet/minecraft/world/entity/monster/RangedAttackMob;")
        run {
            val bowAttackGoal = "net/minecraft/world/entity/ai/goal/RangedBowAttackGoal"
            val bowAttackGoalClone = "xyz/bluspring/kilt/workarounds/RangedBowAttackGoalWorkaround"
            addTransformedClone(
                KiltRemapper.remapClass(bowAttackGoal),
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
                    this.instructions.add(
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL, bowAttackGoalClone,
                            "<init>", "(${mobDesc}DIF)V"
                        )
                    )
                    this.instructions.add(LabelNode())
                    this.instructions.add(InsnNode(Opcodes.RETURN))
                    val endLabel = LabelNode()
                    this.instructions.add(endLabel)

                    this.localVariables.add(
                        LocalVariableNode(
                            "this", "L$bowAttackGoalClone;", "L$bowAttackGoalClone<TT;>;",
                            startLabel, endLabel, 0
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "mob", monsterDesc, "TM;",
                            startLabel, endLabel, 1
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "speedModifier", "D", null,
                            startLabel, endLabel, 2
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "attackIntervalMin", "I", null,
                            startLabel, endLabel, 4
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "attackRadius", "F", null,
                            startLabel, endLabel, 5
                        )
                    )
                }
                classNode.methods.add(fallbackConstructor)
            }
        }

        run {
            val crossbowAttackGoal = "net/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal"
            val crossbowAttackGoalClone = "xyz/bluspring/kilt/workarounds/RangedCrossbowAttackGoalWorkaround"
            addTransformedClone(
                KiltRemapper.remapClass(crossbowAttackGoal),
                crossbowAttackGoalClone,
            ) { classNode ->
                replaceType(classNode, monsterType, mobType)

                val fallbackConstructor = MethodNode(
                    Opcodes.ACC_PUBLIC, "<init>", "(${monsterDesc}DF)V",
                    "<M:$monsterDesc:$rangedAttackMob:${KiltRemapper.remapDescriptor("Lnet/minecraft/world/entity/monster/CrossbowAttackMob;")}>(TM;DF)V", null
                ).apply {
                    val startLabel = LabelNode()
                    this.instructions.add(startLabel)
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
                    this.instructions.add(VarInsnNode(Opcodes.ALOAD, 1))
                    this.instructions.add(VarInsnNode(Opcodes.DLOAD, 2)) // Fun fact, double parameters take 2 variable slots.
                    this.instructions.add(VarInsnNode(Opcodes.FLOAD, 4))
                    this.instructions.add(
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL, crossbowAttackGoalClone,
                            "<init>", "(${mobDesc}DF)V"
                        )
                    )
                    this.instructions.add(LabelNode())
                    this.instructions.add(InsnNode(Opcodes.RETURN))
                    val endLabel = LabelNode()
                    this.instructions.add(endLabel)

                    this.localVariables.add(
                        LocalVariableNode(
                            "this", "L$crossbowAttackGoalClone;", "L$crossbowAttackGoalClone<TT;>;",
                            startLabel, endLabel, 0
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "mob", monsterDesc, "TM;",
                            startLabel, endLabel, 1
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "speedModifier", "D", null,
                            startLabel, endLabel, 2
                        )
                    )
                    this.localVariables.add(
                        LocalVariableNode(
                            "attackRadius", "F", null,
                            startLabel, endLabel, 4
                        )
                    )
                }
                classNode.methods.add(fallbackConstructor)
            }
        }
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
            return Handle(
                handle.tag, newTypeName, handle.name, handle.desc, handle.isInterface
            )
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
        val oldOwnerDesc = "L$oldOwnerName;"
        val newOwnerDesc = "L$newOwnerName;"
        node.name = newOwnerName
        for (method in node.methods) {
            for (insn in method.instructions) {
                when (insn) {
                    is FieldInsnNode -> if (insn.owner == oldOwnerName) insn.owner = newOwnerName
                    is MethodInsnNode -> if (insn.owner == oldOwnerName) insn.owner = newOwnerName
                    is LdcInsnNode -> {
                        val type = insn.cst
                        if (type is Type) {
                            insn.cst = mapType(type, oldOwnerName, newOwnerDesc)
                        }
                    }
                    is TypeInsnNode -> if (insn.desc == oldOwnerName) insn.desc = newOwnerName
                    is InvokeDynamicInsnNode -> {
                        for (i in insn.bsmArgs.indices) {
                            val arg = insn.bsmArgs[i]
                            when (arg) {
                                is Type -> insn.bsmArgs[i] = mapType(arg, oldOwnerName, newOwnerDesc)
                                is Handle -> insn.bsmArgs[i] = mapHandle(arg, oldOwnerName, newOwnerName)
                            }
                        }
                    }
                    is FrameNode -> {
                        insn.local = mapFrameTypes(insn.local, oldOwnerName, newOwnerName)
                        insn.stack = mapFrameTypes(insn.stack, oldOwnerName, newOwnerName)
                    }
                }
            }
            for (local in method.localVariables) {
                if (local.desc == oldOwnerDesc) {
                    local.desc = newOwnerDesc
                }
                local.signature = local.signature?.replace(oldOwnerName, newOwnerName)
            }
            method.signature = method.signature?.replace(oldOwnerName, newOwnerName)
        }
        for (field in node.fields) {
            field.signature = field.signature?.replace(oldOwnerName, newOwnerName)
        }
    }

    fun addTransformedClone(
        targetClass: String, cloneClass: String,
        transformer: (ClassNode) -> Unit
    ) {
        var originalNode: ClassNode? = null
        ClassTinkerers.addPostTransformation(targetClass) { classNode ->
            originalNode = classNode
        }
        val cw = ClassWriter(0)
        cw.visit(
            52,
            Opcodes.ACC_PUBLIC,
            cloneClass,
            null,
            "java/lang/Object",
            null
        )
        ClassTinkerers.define(cloneClass, cw.toByteArray())
        ClassTinkerers.addPostTransformation(cloneClass) { classNode ->
            Class.forName(targetClass.replace("/", ".")) // Classload it somehow so the transform always runs first
            originalNode?.accept(classNode)

            setClassName(classNode, targetClass, cloneClass)

            transformer(classNode)
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
                }
            }
            for (local in method.localVariables) {
                if (local.desc == oldDescriptor) {
                    local.desc = newDescriptor
                }
            }
        }
    }

}
