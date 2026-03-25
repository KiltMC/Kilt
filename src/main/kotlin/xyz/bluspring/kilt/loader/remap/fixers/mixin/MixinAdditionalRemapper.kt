package xyz.bluspring.kilt.loader.remap.fixers.mixin

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.Mixin
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.util.KiltHelper
import kotlin.collections.contains

object MixinAdditionalRemapper {
    val MIXIN_TYPE = Type.getType(Mixin::class.java)
    // Able to match: Lpackage/class/name;methodName(BZLother/type/name;)V
    val MIXIN_METHOD_EXPLICIT_REGEX = Regex("(L(?:\\w+(/)?)*;)\\w+(?:\\((?:Z|B|C|S|I|J|F|D|L(?:\\w+(/)?)*;)*\\)(?:Z|B|C|S|I|J|F|D|V|L(?:\\w+(/)?)*;))?")

    val HARDCODED_REMAPPED_MIXINS = mapOf(
        "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V" to "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
        "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V" to "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"
    )

    fun remapClass(classNode: ClassNode) {
        val mixinAnnotation: AnnotationNode = KiltHelper.mergeNullableCollections(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .firstOrNull { it.desc == MIXIN_TYPE.descriptor }
            ?: throw IllegalStateException("Failed to locate mixin annotations!")
        val values: Map<String, Any> = KiltMixinModifications.annotationValuesToMap(mixinAnnotation.values)
        val targetClassNames = MixinRemapper.getMixinClassTargets(classNode, mixinAnnotation, values)

        // remap some specific mixins
        run {
            for (method in classNode.methods) {
                val annotations = KiltHelper.mergeNullableCollections(method.visibleAnnotations, method.invisibleAnnotations)

                for (node in annotations) {
                    if (node.values == null)
                        continue

                    var wasModified = false
                    val values = KiltMixinModifications.annotationValuesToMap(node.values).toMutableMap()

                    if (values.contains("method")) {
                        val methodValue = values["method"]!!

                        if (methodValue is String) {
                            if (HARDCODED_REMAPPED_MIXINS.containsKey(methodValue)) {
                                values["method"] = HARDCODED_REMAPPED_MIXINS[methodValue]!!
                                wasModified = true
                            }
                        } else if (methodValue is List<*>) {
                            val list = mutableListOf<Any?>()

                            for (value in methodValue) {
                                if (value !is String) {
                                    list.add(value)
                                    continue
                                }

                                if (HARDCODED_REMAPPED_MIXINS.containsKey(value)) {
                                    list.add(HARDCODED_REMAPPED_MIXINS[value]!!)
                                    wasModified = true
                                    continue
                                }

                                list.add(value)
                            }

                            values["method"] = list
                        }
                    }

                    if (wasModified) {
                        node.values = KiltMixinModifications.mapToAnnotationValues(values)
                    }
                }
            }
        }

        fun tryIncreasePriority(intermediaryName: String, mojangName: String, newPriority: Int) {
            if (!values.contains("priority") && (
                        targetClassNames.contains(intermediaryName.replace(".", "/")) || targetClassNames.contains(intermediaryName) ||
                                targetClassNames.contains(mojangName.replace(".", "/")) || targetClassNames.contains(mojangName)
                        )) {
                val modifiedValues = values.toMutableMap()
                modifiedValues["priority"] = newPriority

                if (classNode.visibleAnnotations != null && classNode.visibleAnnotations.any { it.desc == MIXIN_TYPE.descriptor }) {
                    classNode.visibleAnnotations.removeIf { it.desc == MIXIN_TYPE.descriptor }
                    classNode.visibleAnnotations.add(AnnotationNode(Opcodes.ASM9, mixinAnnotation.desc).apply {
                        this.values = KiltMixinModifications.mapToAnnotationValues(modifiedValues)
                    })
                } else if (classNode.invisibleAnnotations != null && classNode.invisibleAnnotations.any { it.desc == MIXIN_TYPE.descriptor }) {
                    classNode.invisibleAnnotations.removeIf { it.desc == MIXIN_TYPE.descriptor }
                    classNode.invisibleAnnotations.add(AnnotationNode(Opcodes.ASM9, mixinAnnotation.desc).apply {
                        this.values = KiltMixinModifications.mapToAnnotationValues(modifiedValues)
                    })
                }
            }
        }

        // Increase priority if LevelRenderer
        run {
            val levelRenderer = FabricLoader.getInstance().mappingResolver.mapClassName("intermediary", "net.minecraft.class_761")
            val levelRendererMoj = "net.minecraft.client.renderer.LevelRenderer"
            tryIncreasePriority(levelRenderer, levelRendererMoj, 1050)
        }

        run {
            // GregTech is mixing into a forge added method that porting lib adds with a priority of 1100
            val level = FabricLoader.getInstance().mappingResolver.mapClassName("intermediary", "net.minecraft.class_1937")
            val levelMoj = "net.minecraft.world.level.Level"
            tryIncreasePriority(level, levelMoj, 1150)
        }

        run {
            // Apotheosis mixin to onSheared
            val sheep = FabricLoader.getInstance().mappingResolver.mapClassName("intermediary", "net.minecraft.class_1472")
            val sheepMoj = "net.minecraft.world.entity.animal.Sheep"
            tryIncreasePriority(sheep, sheepMoj, 1150)
        }

        run {
            val gamemode = FabricLoader.getInstance().mappingResolver.mapClassName("intermediary", "net.minecraft.class_636")
            val gamemodeMoj = "net.minecraft.client.multiplayer.MultiPlayerGameMode"
            if ((
                        targetClassNames.contains(gamemode.replace(".", "/")) || targetClassNames.contains(gamemode) ||
                                targetClassNames.contains(gamemodeMoj.replace(".", "/")) || targetClassNames.contains(gamemodeMoj)
                        ) && classNode.name == "com/gregtechceu/gtceu/core/mixins/client/MultiPlayerGameModeMixin") {

                classNode.methods.remove(classNode.methods[2])
            }
        }
    }
}