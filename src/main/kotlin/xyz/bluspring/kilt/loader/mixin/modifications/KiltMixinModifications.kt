package xyz.bluspring.kilt.loader.mixin.modifications

import com.bawnorton.mixinsquared.TargetHandler
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.sugar.Share
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyVariable
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.transformer.ClassInfo
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AccessorModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier.NameRemappingAnnotationModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier.ReplacedAnnotationsModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.InjectedShareAccessModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.MixinModifier
import xyz.bluspring.kilt.loader.remap.KiltRemapper

object KiltMixinModifications {
    val MIXIN_CLASSES = mutableSetOf<String>()
    private val MODIFIERS = mutableMapOf<String, List<MixinModifier>>()
    private val ACCESSORS = mutableMapOf<String, List<AccessorModifier>>()

    val SUGAR_WRAPPER = Type.getType("Lcom/llamalad7/mixinextras/sugar/impl/SugarWrapper;")

    val INJECT = register(
        Inject::class.java,

        // Fixes Blueprint's ReloadableServerResources
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/server/ReloadableServerResources",
            methods = listOf("loadResources"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"
                ))
            ),
            replaceWith = listOf(
                createAnnotation(
                    TargetHandler::class.java, mapOf(
                        "mixin" to "xyz.bluspring.kilt.forgeinjects.server.ReloadableServerResourcesInject",
                        "name" to "kilt\$blueprintWorkaround"
                    )
                ),
                createAnnotation(
                    Inject::class.java, mapOf(
                        "method" to listOf("@MixinSquared:Handler"),
                        "at" to at("TAIL"),
                        "locals" to arrayOf("Lorg/spongepowered/asm/mixin/injection/callback/LocalCapture;", "CAPTURE_FAILHARD")
                    )
                )
            )
        ),

        // Fixes Rediscovered's Sodium compat
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/client/renderer/LevelRenderer",
            methods = listOf("renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/immediate/CloudRenderer;render",
                    remap = false
                )),
                "require" to 0,
                "cancellable" to true
            ),
            replaceWith = listOf(
                createAnnotation(
                    Inject::class.java, mapOf(
                        "method" to "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V",
                        "at" to at(
                            value = "INVOKE",
                            target = "Lme/jellysquid/mods/sodium/client/render/immediate/CloudRenderer;render(L${KiltRemapper.remapClass("net/minecraft/client/multiplayer/ClientLevel")};L${KiltRemapper.remapClass("net/minecraft/client/player/LocalPlayer")};L${KiltRemapper.remapClass("com/mojang/blaze3d/vertex/PoseStack")};Lorg/joml/Matrix4f;FFDDD)V",
                            remap = false
                        ),
                        "require" to 0,
                        "cancellable" to true
                    )
                )
            )
        ),

        // Disables Structure Gel API's placeInWorld_loadBlockEntity inject,
        // because the locals are all wrong.
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate",
            methods = listOf("placeInWorld"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V",
                    shift = At.Shift.AFTER
                )),
                "locals" to arrayOf("Lorg/spongepowered/asm/mixin/injection/callback/LocalCapture;", "CAPTURE_FAILEXCEPTION")
            ),
            replaceWith = emptyList()
        ),

        // Fixes GTCEu's LevelChunkMixin inject
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/level/chunk/LevelChunk",
            methods = listOf("setBlockState"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z",
                    ordinal = 2
                ))
            ),
            replaceWith = listOf(
                createAnnotation(
                    Inject::class.java, mapOf(
                        "method" to listOf("setBlockState"),
                        "at" to listOf(at(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z",
                            ordinal = 1
                        ))
                    )
                )
            )
        ),

        // Fixes GTCEu's RepairItemRecipeMixin inject
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/item/crafting/RepairItemRecipe",
            methods = listOf("assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "RETURN",
                    ordinal = 1
                )),
                "cancellable" to true
            ),
            replaceWith = listOf(
                createAnnotation(
                    Inject::class.java, mapOf(
                        "method" to listOf("assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"),
                        "at" to listOf(at(
                            value = "INVOKE",
                            target = "Ljava/util/Map;isEmpty()Z",
                            remap = false
                        )),
                        "cancellable" to true
                    )
                )
            ),
        ),

        // Fixes Bedrock Particles mod, but.. because it's an inject, it has an extra param....
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/client/particle/ParticleEngine",
            methods = listOf($$"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"),
            remapMethodsTo = $$"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V"
        ),

        // Fixes Iron's Spells & Spellbooks' ItemStackMixin
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/world/item/ItemStack",
            methods = listOf("<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V"),
            remapMethodsTo = $$"kilt$initItemStackWithTagWorkaround"
        ),

        // Fixes Little Tiles' MinecraftMixin
        InjectedShareAccessModifier(
            owner = "net/minecraft/client/Minecraft",
            methods = listOf("startUseItem", "startUseItem()V"),
            paramToShareMapping = mapOf(
                ParamPair("Lnet/minecraftforge/client/event/InputEvent\$InteractionKeyMappingTriggered;", 0) to Share("inputEvent", namespace = "kilt")
            )
        ),

        // Fixes Forbidden & Arcanus' ScreenMixin (which should really be renamed to GuiGraphicsMixin tbh)
        InjectedShareAccessModifier(
            owner = "net/minecraft/client/gui/GuiGraphics",
            methods = listOf("renderTooltipInternal", "Lnet/minecraft/client/gui/GuiGraphics;renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V"),
            paramToShareMapping = mapOf(
                ParamPair("Lnet/minecraftforge/client/event/RenderTooltipEvent\$Pre;", 0) to Share("preEvent", namespace = "kilt")
            )
        )
    )

    val MODIFY_VARIABLE = register(
        ModifyVariable::class.java,

        // Fixes the Aether's BossHealthOverlay
        ReplacedAnnotationsModifier(
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
                    "prefix" to "wrapWithCondition"
                )),
                createAnnotation(ModifyExpressionValue::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to at("INVOKE", "Lnet/minecraftforge/client/ForgeHooksClient;onCustomizeBossEventProgress${KiltRemapper.remapDescriptor("(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/platform/Window;Lnet/minecraft/client/gui/components/LerpingBossEvent;III)Lnet/minecraftforge/client/event/CustomizeGuiOverlayEvent\$BossEventProgress;")}")
                ))
            )
        ),

        // Fixes Blueprint's StructureTemplate
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate",
            methods = listOf("placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z"),
            variables = mapOf(
                "index" to 22,
                "at" to at(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate\$StructureBlockInfo;nbt:Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
                )
            ),
            replaceWith = listOf(
                createAnnotation(ModifyVariable::class.java, mapOf(
                    "method" to listOf("placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z"),
                    "at" to at(
                        value = "FIELD",
                        target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate\$StructureBlockInfo;nbt:Lnet/minecraft/nbt/CompoundTag;",
                        ordinal = 0
                    ),
                    "index" to 23
                ))
            )
        )
    )

    val ACCESSOR = registerAccessor(
        Accessor::class.java,

        AccessorModifier(
            "net/minecraft/client/color/block/BlockColors",
            listOf("getBlockColors", "blockColors"),
            "()Ljava/util/Map;",

            "xyz/bluspring/kilt/injections/client/color/block/BlockColorsInjection",
            "kilt\$getBlockColors"
        ),
        AccessorModifier(
            "net/minecraft/client/color/item/ItemColors",
            listOf("getItemColors", "itemColors"),
            "()Ljava/util/Map;",

            "xyz/bluspring/kilt/injections/client/color/item/ItemColorsInjection",
            "kilt\$getItemColors"
        ),
        AccessorModifier(
            "net/minecraft/world/level/storage/loot/LootTable",
            listOf("getPools", "pools"),
            "()Ljava/util/List;",

            "xyz/bluspring/kilt/injections/world/level/storage/loot/LootTableInjection",
            "kilt\$getPools"
        )
    )

    val WRAP_OPERATION = register(
        WrapOperation::class.java,

        // Fixes Create's ProjectileUtilMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/entity/projectile/ProjectileUtil",
            methods = listOf("getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"),
            variables = mapOf(
                "at" to listOf(at(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"))
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.forgeinjects.world.entity.projectile.ProjectileUtilInject",
                    "name" to $$"kilt$checkCanRiderInteract",
                    "prefix" to "modifyExpressionValue"
                )),
                createAnnotation(WrapOperation::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to listOf(at(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"))
                ))
            )
        )
    )

    val REDIRECT = register(
        Redirect::class.java,

        // Fixes Forbidden and Arcanus' PlayerMixin
        NameRemappingAnnotationModifier(
            "net/minecraft/world/entity/player/Player",
            methods = listOf("getDigSpeed", "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"),
            remapMethodsTo = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"
        )
    )

    fun findMatchingModifier(className: String, annotation: AnnotationNode, descriptor: String): MixinModifier? {
        var annotation = annotation
        if (annotation.desc == SUGAR_WRAPPER.descriptor) {
            val map = annotationValuesToMap(annotation.values)

            if (map.containsKey("original")) {
                annotation = map["original"] as AnnotationNode
            }
        }

        val modifiers = MODIFIERS[annotation.desc] ?: return null

        for (modifier in modifiers.filter { it.mappedOwner == className }) {
            when (modifier) {
                is AnnotationBasedModifier -> {
                    val map = annotationValuesToMap(annotation.values)

                    if (modifier.methods.none { map["method"] == it || (map["method"] as List<String>).any { a -> a == it } })
                        continue

                    // check if all conditions match
                    if (!checkAllConditionsMatch(modifier.variables, map))
                        continue

                    return modifier
                }

                is InjectedShareAccessModifier -> {
                    val map = annotationValuesToMap(annotation.values)

                    if (modifier.methods.none { map["method"] == it || (map["method"] as List<String>).any { a -> a == it } })
                        continue

                    val descriptor = Type.getArgumentTypes(descriptor)

                    for ((paramPair, _) in modifier.paramToShareMapping) {
                        val (param, ordinal) = paramPair

                        if (descriptor.count { it.descriptor == param } < ordinal) {
                            return null
                        }
                    }

                    return modifier
                }
            }
        }

        return null
    }

    fun findMatchingAccessor(classInfo: ClassInfo, annotation: AnnotationNode, methodNode: MethodNode): AccessorModifier? {
        val modifiers = ACCESSORS[annotation.desc] ?: return null

        for (modifier in modifiers.filter { it.mappedOwner == classInfo.name }) {
            val map = annotationValuesToMap(annotation.values ?: emptyList())

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
                        value.any { b ->
                            if (b is AnnotationNode && a is AnnotationNode)
                                checkAllConditionsMatch(annotationValuesToMap(b.values), annotationValuesToMap(a.values))
                            else
                                b == a
                        }
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
                else if (value is Array<*> && it.value is Array<*>) {
                    var current = true
                    val value2 = it.value as Array<*>

                    for ((index, i) in value.withIndex()) {
                        if (i != value2[index]) {
                            current = false
                        }
                    }

                    current
                } else if (value is AnnotationNode)
                    if (it.value is Map<*, *>)
                        checkAllConditionsMatch(annotationValuesToMap(value.values), it.value as Map<String, Any>)
                    else false
                else
                    value == it.value
        }
    }

    fun createAnnotation(annotationType: Class<*>, variables: Map<String, Any>): AnnotationNode {
        return createAnnotation(Type.getDescriptor(annotationType), variables)
    }

    fun createAnnotation(annotationType: String, variables: Map<String, Any>): AnnotationNode {
        return AnnotationNode(annotationType).apply {
            this.values = mapToAnnotationValues(variables)
        }
    }

    fun mapToAnnotationValues(map: Map<String, Any>): List<Any> {
        val values = mutableListOf<Any>()

        for ((key, v) in map) {
            values.add(key)
            values.add(v)
        }

        return values
    }

    fun annotationValuesToMap(values: List<Any>): Map<String, Any> {
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

    private fun at(value: String, target: String? = null, variables: Map<String, Any> = mapOf(), ordinal: Int? = null, remap: Boolean? = null, shift: At.Shift? = null): AnnotationNode {
        return createAnnotation(At::class.java, mutableMapOf<String, Any>(
            "value" to value
        ).apply {
            if (target != null)
                this["target"] = target

            if (ordinal != null)
                this["ordinal"] = ordinal

            if (remap != null)
                this["remap"] = remap

            if (shift != null)
                this["shift"] = arrayOf("Lorg/spongepowered/asm/mixin/injection/At\$Shift;", shift.name)

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