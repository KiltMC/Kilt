package xyz.bluspring.kilt.loader.remap.fixers.mixin

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.reflect.Modifier

object MixinStaticMethodFixer {
    private val STATIC_METHODS = mapOf(
        "net/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity" to listOf(
            "canBurn", "burn", "canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z", "burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"
        )
    )

    // Forge switches some methods from static to instance.
    // We need to fix the mixin classes that are targeting them.
    fun fixClass(classNode: ClassNode) {
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode)

        if (STATIC_METHODS.none { targetClassNames.contains(it.key) || targetClassNames.contains(KiltRemapper.remapClass(it.key)) })
            return

        val staticMethods = STATIC_METHODS.filter { targetClassNames.contains(it.key) || targetClassNames.contains(KiltRemapper.remapClass(it.key)) }.values.first()
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
            for (node in methodNode.instructions) {
                if (node is VarInsnNode) {
                    node.`var` = node.`var` - 1
                    newInstructions.add(node)
                } else {
                    newInstructions.add(node)
                }
            }

            methodNode.instructions = newInstructions

            if (methodNode.localVariables != null) {
                val newLocals = mutableListOf<LocalVariableNode>()

                for (local in methodNode.localVariables) {
                    if (local.name == "this")
                        continue

                    local.index = local.index - 1
                    newLocals.add(local)
                }

                methodNode.localVariables = newLocals
                methodNode.maxLocals = methodNode.maxLocals - 1
            }
        }
    }
}