package xyz.bluspring.kilt.loader.mixin.modifier

import com.bawnorton.mixinsquared.TargetHandler
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable
import org.spongepowered.asm.mixin.transformer.ClassInfo
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object KiltMixinModifications {
    val MIXIN_CLASSES = mutableSetOf<String>()
    private val MODIFIERS = mutableMapOf<String, List<MixinModifier>>()
    private val ACCESSORS = mutableMapOf<String, List<AccessorModifier>>()

    val MODIFY_VARIABLE = register(
        ModifyVariable::class.java,

        // Fixes the Aether's BossHealthOverlay
        MixinModifier(
            owner = "net/minecraft/client/gui/components/BossHealthOverlay",
            methods = listOf("render(Lnet/minecraft/client/gui/GuiGraphics;)V"),
            variables = mapOf(
                "index" to 7,
                "at" to at("STORE")
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.forgeinjects.client.gui.components.BossHealthOverlayInject",
                    "name" to "kilt\$customizeBossEventProgress",
                    "prefix" to "handler"
                )),
                createAnnotation(ModifyExpressionValue::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to at("INVOKE", "Lnet/minecraftforge/client/ForgeHooksClient;onCustomizeBossEventProgress${KiltRemapper.remapDescriptor("(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/platform/Window;Lnet/minecraft/client/gui/components/LerpingBossEvent;III)Lnet/minecraftforge/client/event/CustomizeGuiOverlayEvent\$BossEventProgress;")}")
                ))
            )
        )
    )

    val ACCESSOR = registerAccessor(
        Accessor::class.java,

        AccessorModifier(
            "net/minecraft/client/color/block/BlockColors",
            listOf("getBlockColors", "blockColors"),
            "()Ljava/util/Map;"
        ) { owner ->
            MethodNode().apply {
                visitCode()

                val label0 = Label()
                val label1 = Label()

                visitLabel(label0)

                visitVarInsn(Opcodes.ALOAD, 0)
                visitTypeInsn(
                    Opcodes.CHECKCAST,
                    "xyz/bluspring/kilt/injections/client/color/block/BlockColorsInjection"
                )
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "xyz/bluspring/kilt/injections/client/color/block/BlockColorsInjection",
                    "kilt\$getBlockColors", "()Ljava/util/Map;", true
                )
                visitInsn(Opcodes.ARETURN)

                visitLabel(label1)
                visitLocalVariable("this", "L${owner};", null, label0, label1, 0)
                visitMaxs(1, 1)

                visitEnd()
            }
        }
    )

    fun findMatchingModifier(classInfo: ClassInfo, annotation: AnnotationNode): MixinModifier? {
        val modifiers = MODIFIERS[annotation.desc] ?: return null

        for (modifier in modifiers.filter { it.mappedOwner == classInfo.name }) {
            val map = annotationValuesToMap(annotation.values)

            if (modifier.methods != null && modifier.methods.none { map["method"] == it || (map["method"] as List<String>).any { a -> a == it } })
                continue

            // check if all conditions match
            if (!checkAllConditionsMatch(modifier.variables, map))
                continue

            return modifier
        }

        return null
    }

    fun findMatchingAccessor(classInfo: ClassInfo, annotation: AnnotationNode, methodNode: MethodNode): AccessorModifier? {
        val modifiers = ACCESSORS[annotation.desc] ?: return null

        for (modifier in modifiers.filter { it.mappedOwner == classInfo.name }) {
            val map = annotationValuesToMap(annotation.values)

            if (modifier.names.none { it == methodNode.name } && ((map.containsKey("value") && modifier.names.none { it == map["value"] }) || !map.containsKey("value")))
                continue

            if (methodNode.desc != KiltRemapper.remapDescriptor(modifier.desc))
                continue

            return modifier
        }

        return null
    }

    private fun checkAllConditionsMatch(map1: Map<String, Any>, map: Map<String, Any>): Boolean {
        return map1.all {
            val value = map[it.key]

            // i know, this looks awful
            return@all if (it.value is List<*>)
                (it.value as List<*>).any { a ->
                    if (value is List<*>)
                        value.any { b -> b == a }
                    else if (value is AnnotationNode)
                        if (a is AnnotationNode)
                            checkAllConditionsMatch(annotationValuesToMap(value.values), annotationValuesToMap(a.values))
                        else if (a is Map<*, *>)
                            checkAllConditionsMatch(annotationValuesToMap(value.values), a as Map<String, Any>)
                        else false
                    else
                        a == value
                }
            else if (it.value is AnnotationNode)
                if (value is AnnotationNode)
                    checkAllConditionsMatch(annotationValuesToMap((it.value as AnnotationNode).values), annotationValuesToMap(value.values))
                else if (value is Map<*, *>)
                    checkAllConditionsMatch(annotationValuesToMap((it.value as AnnotationNode).values), value as Map<String, Any>)
                else false
            else
                // check if values != equal and value is not list
                // or if value is list, check if none of the values equal the main value
                if (value is List<*>)
                    value.any { a -> a == it.value }
                else if (value is AnnotationNode)
                    if (it.value is Map<*, *>)
                        checkAllConditionsMatch(annotationValuesToMap(value.values), it.value as Map<String, Any>)
                    else false
                else
                    value == it.value
        }
    }

    private fun createAnnotation(annotationType: Class<*>, variables: Map<String, Any>): AnnotationNode {
        return AnnotationNode(Type.getDescriptor(annotationType)).apply {
            val values = mutableListOf<Any>()

            for ((key, v) in variables) {
                values.add(key)
                values.add(v)
            }

            this.values = values
        }
    }

    private fun annotationValuesToMap(values: List<Any>): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        var currentKey = ""
        for ((index, value) in values.withIndex()) {
            if ((index and 1) == 0) {
                currentKey = value as String
            } else {
                map[currentKey] = value
            }
        }

        return map
    }

    private fun at(value: String, target: String? = null, variables: Map<String, Any> = mapOf()): AnnotationNode {
        return createAnnotation(At::class.java, mutableMapOf<String, Any>(
            "value" to value
        ).apply {
            if (target != null)
                this["target"] = target

            this.putAll(variables)
        })
    }

    fun register(type: Class<*>, vararg mixinModifiers: MixinModifier): List<MixinModifier> {
        val list = mutableListOf<MixinModifier>()
        val typeDesc = Type.getDescriptor(type)

        for (modifier in mixinModifiers) {
            val owner = KiltRemapper.remapClass(modifier.owner)
            MIXIN_CLASSES.add(owner)
            modifier.mappedOwner = owner
            list.add(modifier)
        }

        MODIFIERS[typeDesc] = list
        return list
    }

    fun registerAccessor(type: Class<*>, vararg accessorModifiers: AccessorModifier): List<AccessorModifier> {
        val list = mutableListOf<AccessorModifier>()
        val typeDesc = Type.getDescriptor(type)

        for (modifier in accessorModifiers) {
            val owner = KiltRemapper.remapClass(modifier.owner)
            val desc = KiltRemapper.remapDescriptor(modifier.desc)
            MIXIN_CLASSES.add(owner)
            modifier.mappedOwner = owner
            modifier.mappedDesc = desc

            list.add(modifier)
        }

        ACCESSORS[typeDesc] = list
        return list
    }
}