package xyz.bluspring.kilt.loader.mixin.modifications

import com.bawnorton.mixinsquared.TargetHandler
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.sugar.Local
import com.llamalad7.mixinextras.sugar.Share
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyVariable
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.transformer.ClassInfo
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.*
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier.NameRemappingAnnotationModifier
import xyz.bluspring.kilt.loader.mixin.modifications.modifiers.AnnotationBasedModifier.ReplacedAnnotationsModifier
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinRemapper

object KiltMixinModifications {
    val MIXIN_CLASSES = mutableSetOf<String>()
    private val MODIFIERS = mutableMapOf<String, List<MixinModifier>>()
    private val ACCESSORS = mutableMapOf<String, List<AccessorModifier>>()

    val SUGAR_WRAPPER = Type.getType("Lcom/llamalad7/mixinextras/sugar/impl/SugarWrapper;")
    val FACTORY_REDIRECT_WRAPPER = Type.getType("Lcom/llamalad7/mixinextras/wrapper/factory/FactoryRedirectWrapper;")
    val CALLBACK_INFO = Type.getType(CallbackInfo::class.java)
    val CALLBACK_INFO_RETURNABLE = Type.getType(CallbackInfoReturnable::class.java)

    val GLOBAL_MODIFIERS = listOf<MixinModifier>(
        RetargetingLocalModifier(
            // Fixes Create Aeronautics
            "net/minecraft/client/MouseHandler",
            listOf("turnPlayer", "turnPlayer(D)V"),
            mapOf(
                LocalPair("D", Local(ordinal = 4)) to Local(ordinal = 1),
                LocalPair("D", Local(ordinal = 5)) to Local(ordinal = 2),
                LocalPair("D", Local(ordinal = 1)) to Local(ordinal = 4),
                LocalPair("D", Local(ordinal = 2)) to Local(ordinal = 5),
                // https://github.com/Alchemists-Of-Yore/No-Mans-Land/blob/1.21.1/src/main/java/com/farcr/nomansland/common/mixin/client/MouseHandlerMixin.java#L25
                LocalPair("D", Local(name = arrayOf("d0"))) to Local(ordinal = 1),
                LocalPair("D", Local(name = arrayOf("d1"))) to Local(ordinal = 2)
            )
        ),
        RetargetingLocalModifier(
            // another Aeronautics fix
            "net/minecraft/world/level/block/ComparatorBlock",
            listOf("getInputSignal", "getInputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I"),
            mapOf(
                LocalPair("Lnet/minecraft/core/Direction;", Local(name = arrayOf("direction"))) to Local(ordinal = 0)
            )
        ),

        // Fix No Man's Land ServerPlayerMixin
        // https://github.com/Alchemists-Of-Yore/No-Mans-Land/blob/1.21.1/src/main/java/com/farcr/nomansland/common/mixin/ServerPlayerMixin.java#L35-L41
        // This lambda is added by NeoForge and contains some of the logic. Luckily for us, they have the same signature.
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/server/level/ServerPlayer",
            methods = listOf($$"lambda$startSleepInBed$13", $$"lambda$startSleepInBed$13(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"),
            remapMethodsTo = listOf("startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;")
        ),

        // Fix Blueprint's PlayerRendererMixin https://github.com/team-abnormals/blueprint/blob/1.21.x/src/main/java/com/teamabnormals/blueprint/core/mixin/client/PlayerRendererMixin.java#L20
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/client/renderer/entity/player/PlayerRenderer",
            methods = listOf("renderNameTag"),
            remapMethodsTo = listOf(
                "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
                "renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V"
            )
        ),

        // Fixes KubeJS's MinecraftServerMixin
        // https://github.com/KubeJS-Mods/KubeJS/blob/8f62bc159b7132f43570b0e55713177eb382a220/src/main/java/dev/latvian/mods/kubejs/core/mixin/MinecraftServerMixin.java#L144-L147
        NameRemappingAnnotationModifier(
            "net/minecraft/server/MinecraftServer",
            methods = listOf($$"lambda$reloadResources$29", $$"lambda$reloadResources$29(Lcom/google/common/collect/ImmutableList;)Ljava/util/concurrent/CompletionStage;"),
            remapMethodsTo = listOf($$"lambda$reloadResources$28(Lcom/google/common/collect/ImmutableList;)Ljava/util/concurrent/CompletionStage;")
        ),

        // Fixes Lodestone's GuiMixin
        NameRemappingAnnotationModifier(
            "net/minecraft/client/gui/Gui",
            methods = listOf("renderHotbar", "renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
            remapMethodsTo = listOf("renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V") // good enough tm
        )
    )

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
                        "mixin" to "xyz.bluspring.kilt.injects.server.ReloadableServerResourcesInject",
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
            remapMethodsTo = listOf($$"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V")
        ),

        // Fixes Iron's Spells & Spellbooks' ItemStackMixin
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/world/item/ItemStack",
            methods = listOf("<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V"),
            remapMethodsTo = listOf($$"kilt$initItemStackWithTagWorkaround")
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
        ),

        // Fixes Create's HumanoidArmorLayerMixin
        InjectedShareAccessModifier(
            owner = "net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer",
            methods = listOf("renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"),
            paramToShareMapping = mapOf(
                ParamPair("F", 0) to Share("limbSwing", namespace = Kilt.MOD_ID),
                ParamPair("F", 1) to Share("limbSwingAmount", namespace = Kilt.MOD_ID),
                ParamPair("F", 2) to Share("partialTick", namespace = Kilt.MOD_ID),
                ParamPair("F", 3) to Share("ageInTicks", namespace = Kilt.MOD_ID),
                ParamPair("F", 4) to Share("netHeadYaw", namespace = Kilt.MOD_ID),
                ParamPair("F", 5) to Share("headPitch", namespace = Kilt.MOD_ID),
            )
        ),

        // Goes along with the above
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer",
            methods = listOf("renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"),
            remapMethodsTo = listOf("renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V")
        ),

        // Fixes Quark's ServerEntityMixin
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/server/level/ServerEntity",
            methods = listOf("sendPairingData", "sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Lnet/neoforged/neoforge/network/bundle/PacketAndPayloadAcceptor;)V",
                "method_18757"
            ),
            remapMethodsTo = listOf($$"neoforge$sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Lnet/neoforged/neoforge/network/bundle/PacketAndPayloadAcceptor;)V")
        ),

        // Fixes TerraFirmaCraft's MinecraftMixin, and probably some others too.
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/client/Client",
            methods = listOf($$"*(Lnet/minecraft/client/Minecraft$GameLoadCookie;)V", $$"lambda$new$7", $$"lambda$new$7(Lnet/minecraft/client/Minecraft$GameLoadCookie;)V"),
            remapMethodsTo = listOf($$"method_29339(Ljava/util/concurrent/CompletableFuture;Lnet/minecraft/client/Minecraft$GameLoadCookie;)V")
        ),

        // Fixes TerraFirmaCraft's ServerPlayerGameModeMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/server/level/ServerPlayerGameMode",
            methods = listOf("destroyBlock", "destroyBlock(Lnet/minecraft/core/BlockPos;)Z"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    remap = false
                ))
            ),
            replaceWith = listOf(
                createAnnotation(
                    Inject::class.java, mapOf(
                        "method" to listOf("destroyBlock(Lnet/minecraft/core/BlockPos;)Z"),
                        "at" to listOf(at(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
                        ))
                    )
                )
            )
        ),

        // Fixes Lodestone's ShaderInstanceMixin
        InjectedShareAccessModifier(
            owner = "net/minecraft/client/renderer/ShaderInstance",
            methods = listOf("<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V"),
            paramToShareMapping = mapOf(
                ParamPair("Lcom/mojang/blaze3d/vertex/VertexFormat;", 0) to Share("vertexFormat", namespace = Kilt.MOD_ID),
                ParamPair("Lnet/minecraft/resources/ResourceLocation;", 0) to Share("shaderLocation", namespace = Kilt.MOD_ID),
                ParamPair("Lnet/minecraft/server/packs/resources/ResourceProvider;", 0) to Share("resourceProvider", namespace = Kilt.MOD_ID),
            )
        ),

        // Goes along with the above
        NameRemappingAnnotationModifier(
            owner = "net/minecraft/client/renderer/ShaderInstance",
            methods = listOf("<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V"),
            remapMethodsTo = listOf("<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)V")
        ),

        // Fixes Hexerei's KeyboardHandlerMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/client/KeyboardHandler",
            methods = listOf("keyPress", "keyPress(JIIII)V"),
            variables = mapOf(
                "at" to listOf(at(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onKeyInput(IIII)V")),
                "cancellable" to true
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.injects.client.KeyboardHandlerInject",
                    "name" to $$"kilt$workaround$handleKeyPressInject"
                )),
                createAnnotation(Inject::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to at("HEAD"),
                    "cancellable" to true
                ))
            )
        ),
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
                    "mixin" to "xyz.bluspring.kilt.injects.client.gui.components.BossHealthOverlayInject",
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
        ),

        // Fixes Lodestone's BlockItemMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/item/BlockItem",
            methods = listOf("place", "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/sounds/SoundEvent;"
                ))
            ),
            replaceWith = listOf(
                createAnnotation(
                    ModifyVariable::class.java, mapOf(
                        "method" to listOf("place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
                        "at" to listOf(at(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"
                        ))
                    )
                )
            )
        ),
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
            "net/minecraft/client/particle/ParticleEngine",
            listOf("getProviders", "providers"),
            "()Ljava/util/Map;",

            "xyz/bluspring/kilt/injections/client/particle/ParticleEngineInjection",
            "kilt\$getProviders"
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
                    "mixin" to "xyz.bluspring.kilt.injects.world.entity.projectile.ProjectileUtilInject",
                    "name" to $$"kilt$checkCanRiderInteract",
                    "prefix" to "modifyExpressionValue"
                )),
                createAnnotation(WrapOperation::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to listOf(at(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"))
                ))
            )
        ),

        // Fixes Quark's EnchantmentHelperMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/item/enchantment/EnchantmentHelper",
            methods = listOf($$"runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = $$"Lnet/minecraft/world/item/ItemStack;getAllEnchantments(Lnet/minecraft/core/HolderLookup$RegistryLookup;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"
                ))
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.injects.world.item.enchantment.EnchantmentHelperInject",
                    "name" to $$"kilt$tryGetAllEnchantmentsFromLookup"
                )),
                createAnnotation(WrapOperation::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to listOf(at(
                        value = "INVOKE",
                        target = $$"Lnet/minecraft/world/item/ItemStack;getAllEnchantments(Lnet/minecraft/core/HolderLookup$RegistryLookup;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"
                    ))
                ))
            )
        ),

        // Fixes Quark's EnchantmentHelperMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/world/item/enchantment/EnchantmentHelper",
            methods = listOf($$"runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V"),
            variables = mapOf(
                "at" to listOf(at(
                    value = "INVOKE",
                    target = $$"Lnet/minecraft/world/item/ItemStack;getAllEnchantments(Lnet/minecraft/core/HolderLookup$RegistryLookup;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"
                ))
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.injects.world.item.enchantment.EnchantmentHelperInject",
                    "name" to $$"kilt$tryGetAllEnchantments"
                )),
                createAnnotation(WrapOperation::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to listOf(at(
                        value = "INVOKE",
                        target = $$"Lnet/minecraft/world/item/ItemStack;getAllEnchantments(Lnet/minecraft/core/HolderLookup$RegistryLookup;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"
                    ))
                ))
            )
        )
    )

    val WRAP_WITH_CONDITION = register(
        WrapWithCondition::class.java,

        // Fixes Iron's Lib's HumanoidArmorLayerMixin
        ReplacedAnnotationsModifier(
            owner = "net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer",
            methods = listOf("render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"),
            variables = mapOf(
                "at" to listOf(at(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"))
            ),
            replaceWith = listOf(
                createAnnotation(TargetHandler::class.java, mapOf(
                    "mixin" to "xyz.bluspring.kilt.injects.client.renderer.entity.layers.HumanoidArmorLayerInject",
                    "name" to $$"kilt$tryHandleRenderArmorPieceCompatibility",
                    "prefix" to "wrapOperation"
                )),
                createAnnotation(WrapWithCondition::class.java, mapOf(
                    "method" to listOf("@MixinSquared:Handler"),
                    "at" to listOf(at(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"))
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
            remapMethodsTo = listOf("getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F")
        )
    )

    fun getBaseAnnotation(annotation: AnnotationNode): AnnotationNode {
        var annotation = annotation
        if (annotation.desc == SUGAR_WRAPPER.descriptor || annotation.desc == FACTORY_REDIRECT_WRAPPER.descriptor) {
            val map = annotationValuesToMap(annotation.values)

            if (map.containsKey("original")) {
                annotation = map["original"] as AnnotationNode
            }
        }

        return annotation
    }

    fun findMatchingModifiers(className: String, annotation: AnnotationNode, descriptor: String): Collection<MixinModifier> {
        val annotation = getBaseAnnotation(annotation)

        val modifiers = (MODIFIERS[annotation.desc] ?: emptyList()) + GLOBAL_MODIFIERS
        val foundModifiers = mutableListOf<MixinModifier>()
        val map = annotationValuesToMap(annotation.values ?: emptyList())

        modifierSearch@for (modifier in modifiers.filter { it.mappedOwner == className || it.owner == className }) {
            when (modifier) {
                is AnnotationBasedModifier -> {
                    if (!modifier.matches(map["method"] ?: continue)) {
                        continue
                    }

                    // check if all conditions match
                    if (!checkAllConditionsMatch(modifier.variables, map)) {
                        continue
                    }

                    foundModifiers.add(modifier)
                }

                is InjectedShareAccessModifier -> {
                    if (!modifier.matches(map["method"] ?: continue)) {
                        continue
                    }

                    val descriptor = Type.getArgumentTypes(descriptor)

                    for ((paramPair, _) in modifier.paramToShareMapping) {
                        val (param, ordinal) = paramPair
                        val mappedParam = KiltRemapper.remapDescriptor(param)

                        if (descriptor.count { it.descriptor == param || it.descriptor == mappedParam || KiltRemapper.remapDescriptor(it.descriptor) == param || KiltRemapper.remapDescriptor(it.descriptor) == mappedParam } < ordinal + 1) {
                            continue@modifierSearch
                        }
                    }

                    foundModifiers.add(modifier)
                }

                // must be below everything!
                is MethodBasedModifier -> {
                    if (!modifier.matches(map["method"] ?: continue)) {
                        continue
                    }

                    foundModifiers.add(modifier)
                }
            }
        }

        return foundModifiers
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
                this["target"] = MixinRemapper.remapTargetString(target, emptyList(), KiltRemapper.enhancedRemapper)

            if (ordinal != null)
                this["ordinal"] = ordinal

            if (remap != null)
                this["remap"] = remap

            if (shift != null)
                this["shift"] = arrayOf("Lorg/spongepowered/asm/mixin/injection/At\$Shift;", shift.name)

            this.putAll(variables)
        })
    }

    init {
        for (modifier in GLOBAL_MODIFIERS) {
            finalizeModifier(modifier)
        }
    }

    fun finalizeModifier(modifier: MixinModifier) {
        val owner = KiltRemapper.remapClass(modifier.owner)
        MIXIN_CLASSES.add(owner)
        modifier.mappedOwner = owner

        if (modifier is MethodBasedModifier) {
            modifier.mappedMethods = modifier.methods.map {
                (if (it.contains("(")) {
                    val name = it.replaceAfter("(", "").removeSuffix("(")
                    val descriptor = it.removePrefix(name)
                    val mappedDesc = KiltRemapper.remapDescriptor(descriptor)

                    "${KiltRemapper.mojMappedMethods[name]?.get(modifier.owner)?.firstOrNull { m -> m.second == mappedDesc }?.first ?: name}$mappedDesc"
                } else KiltRemapper.mojMappedMethods[it]?.get(modifier.owner)?.firstOrNull()?.first ?: it)
            }
        }

        if (modifier is NameRemappingAnnotationModifier) {
            modifier.remapMethodsTo = modifier.remapMethodsTo.map {
                MixinRemapper.remapTargetString(
                    it, listOf(KiltRemapper.unmapClass(modifier.owner)),
                    KiltRemapper.enhancedRemapper
                )
            }
        }
        if (modifier is ReplacedAnnotationsModifier) {
            val mutableList = modifier.replaceWith.toMutableList()
            MixinRemapper.remapMixinAnnotations(
                mutableList, KiltRemapper.enhancedRemapper,
                listOf(modifier.owner), modifier.owner
            )
            modifier.replaceWith = mutableList
        }
    }

    fun register(type: Class<*>, vararg mixinModifiers: MixinModifier): List<MixinModifier> {
        val list = mutableListOf<MixinModifier>()
        val typeDesc = Type.getDescriptor(type)

        for (modifier in mixinModifiers) {
            finalizeModifier(modifier)

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
