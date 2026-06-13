package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.reflect.Modifier

object MixinStaticMethodFixer {
    private val STATIC_METHODS = mapOf(
        "net/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity" to listOf(
            "canBurn", "burn", "canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z", "burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"
        )
    )

    private val THIS_REMAP = mapOf(
        "net/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity" to "kilt\$furnaceBE"
    )

    // Forge switches some methods from static to instance.
    // We need to fix the mixin classes that are targeting them.
    fun fixClass(classNode: ClassNode) {
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)

        if (STATIC_METHODS.none { targetClassNames.contains(it.key) || targetClassNames.contains((it.key)) })
            return

        val staticMethods = STATIC_METHODS.filter { targetClassNames.contains(it.key) || targetClassNames.contains((it.key)) }.values.first()
        val markedAsStatic = mutableListOf<MethodNode>()

        for (methodNode in classNode.methods) {
            if (Modifier.isStatic(methodNode.access))
                continue

            val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)

            for (annotationNode in annotations) {
                if (annotationNode.values == null || annotationNode.values.isEmpty())
                    continue

                val values = KiltMixinModifications.annotationValuesToMap(annotationNode.values)
                if (values.contains("method")) {
                    val methodValue = values["method"]

                    if (methodValue is String) {
                        if (staticMethods.contains(methodValue)) {
                            methodNode.access = methodNode.access or Opcodes.ACC_STATIC
                            markedAsStatic.add(methodNode)
                            break
                        }
                    } else if (methodValue is List<*>) {
                        for (value in methodValue) {
                            if (value !is String)
                                continue

                            if (staticMethods.contains(value)) {
                                methodNode.access = methodNode.access or Opcodes.ACC_STATIC
                                markedAsStatic.add(methodNode)
                                break
                            }
                        }
                    }
                }
            }
        }

        for (methodNode in markedAsStatic) {
            val newInstructions = InsnList()
            val thisRemap = THIS_REMAP.filter { targetClassNames.contains(it.key) || targetClassNames.contains((it.key)) }.values.firstOrNull()

            val className = targetClassNames.first() // please tell me people are normal about this
            var firstLabel = LabelNode(Label())
            lateinit var lastLabel: LabelNode
            var hasSetFirstLabel = false

            // If there exists a "this" remap, let's try to use it.
            if (thisRemap != null && methodNode.localVariables != null) {
                newInstructions.add(firstLabel)
                newInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, className, thisRemap, "Ljava/lang/ThreadLocal;"))
                newInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;", false))
                newInstructions.add(VarInsnNode(Opcodes.ASTORE, methodNode.localVariables.size - 1))

                hasSetFirstLabel = true
            }

            for (node in methodNode.instructions) {
                if (node is VarInsnNode) {
                    if (node.`var` == 0)
                        node.`var` = methodNode.localVariables.size - 1
                    else
                        node.`var` = node.`var` - 1
                } else if (node is LabelNode) {
                    if (!hasSetFirstLabel) {
                        firstLabel = node
                        hasSetFirstLabel = true
                    }

                    lastLabel = node
                }

                newInstructions.add(node)
            }

            methodNode.instructions = newInstructions

            if (methodNode.localVariables != null && thisRemap == null) {
                val newLocals = mutableListOf<LocalVariableNode>()

                // Move all locals back by one, as no "this" label exists.
                for (local in methodNode.localVariables) {
                    if (local.name == "this")
                        continue

                    local.index -= 1
                    newLocals.add(local)
                }

                methodNode.localVariables = newLocals
                methodNode.maxLocals -= 1
            } else if (methodNode.localVariables != null) {
                val newLocals = mutableListOf<LocalVariableNode>()

                // Move all locals back by one, and add another start label
                for (local in methodNode.localVariables) {
                    if (local.name == "this")
                        continue

                    local.start = firstLabel
                    local.index -= 1
                    newLocals.add(local)
                }

                // Now add the "this" variable under a new index
                newLocals.add(LocalVariableNode("this", "L$className;", null, firstLabel, lastLabel, newLocals.size))

                methodNode.localVariables = newLocals
            }
        }
    }
}
