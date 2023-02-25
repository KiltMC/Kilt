package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import xyz.bluspring.kilt.Kilt

class KiltEarlyRiser : Runnable {
    private val namespace = FabricLauncherBase.getLauncher().targetNamespace

    override fun run() {
        // EnchantmentCategory has an abstract method that doesn't exactly play nicely with enum extension.
        // So, we need to modify it to have a method body that works with the Forge API.
        run {
            // The remapper wouldn't fucking remap, might as well do it manually - Kilt isn't supposed to run
            // in other mod devs' environments anyway.
            val enchantmentCategory = namespaced("net.minecraft.class_1886", "net.minecraft.world.item.enchantment.EnchantmentCategory")

            ClassTinkerers.addTransformation(enchantmentCategory) { classNode ->
                classNode.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_ENUM

                run {
                    val canEnchantName = namespaced("method_8177", "canEnchant")
                    // remove it first
                    classNode.methods.removeIf { it.name == canEnchantName }

                    val item = namespaced("net.minecraft.class_1792", "net.minecraft.world.item.Item")
                    val enchCategoryInjection = "xyz/bluspring/kilt/injections/world/item/enchantment/EnchantmentCategoryInjection"

                    // This method should become:
                    /*
                    public boolean canEnchant(Item item) {
                        return ((EnchantmentCategoryInjection) this).getPredicate() != null && ((EnchantmentCategoryInjection) this).getPredicate().test(item);
                    }
                     */
                    val canEnchant = classNode.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        canEnchantName, "(L${item.replace(".", "/")};)Z",
                        null, null
                    )

                    canEnchant.visitCode()

                    val label0 = Label()
                    val label1 = Label()
                    val label2 = Label()
                    val label3 = Label()

                    canEnchant.visitLabel(label0)
                    // if (((EnchantmentCategoryInjection) this).getDelegate() != null)
                    canEnchant.visitVarInsn(Opcodes.ALOAD, 0)
                    canEnchant.visitTypeInsn(Opcodes.CHECKCAST, enchCategoryInjection)
                    canEnchant.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        enchCategoryInjection,
                        "getDelegate",
                        "()Ljava/util/function/Predicate;",
                        true
                    )
                    canEnchant.visitJumpInsn(Opcodes.IFNULL, label1)

                    // ((EnchantmentCategoryInjection) this).getDelegate().test(item)
                    canEnchant.visitVarInsn(Opcodes.ALOAD, 0)
                    canEnchant.visitTypeInsn(Opcodes.CHECKCAST, enchCategoryInjection)
                    canEnchant.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        enchCategoryInjection,
                        "getDelegate",
                        "()Ljava/util/function/Predicate;",
                        true
                    )
                    canEnchant.visitVarInsn(Opcodes.ALOAD, 1)
                    canEnchant.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        "java/util/function/Predicate",
                        "test",
                        "(Ljava/lang/Object;)Z",
                        true
                    )
                    canEnchant.visitJumpInsn(Opcodes.IFEQ, label1)
                    canEnchant.visitInsn(Opcodes.ICONST_1)
                    canEnchant.visitJumpInsn(Opcodes.GOTO, label2)

                    // &&
                    canEnchant.visitLabel(label1)
                    canEnchant.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
                    canEnchant.visitInsn(Opcodes.ICONST_0)

                    canEnchant.visitLabel(label2)
                    canEnchant.visitFrame(Opcodes.F_SAME1, 0, null, 1, arrayOf(Opcodes.INTEGER))
                    canEnchant.visitInsn(Opcodes.IRETURN)

                    canEnchant.visitLabel(label3)
                    canEnchant.visitLocalVariable(
                        "this",
                        "L${enchantmentCategory.replace(".", "/")};",
                        null,
                        label0,
                        label3,
                        0
                    )
                    canEnchant.visitLocalVariable("item", "L${item.replace(".", "/")};", null, label0, label3, 1)

                    canEnchant.visitMaxs(0, 0)

                    canEnchant.visitEnd()
                }
            }
        }

        // We need to add some new initializers because thanks Forge.
        // I probably should've done this from the beginning, honestly
        // TODO: If it's possible, we should make this annotation-based utilizing Mixin.
        //       Makes life so much easier.
        run {
            /*
            c net/minecraft/client/renderer/block/model/ItemTransform xyz/bluspring/kilt/injections/client/render/block/model/ItemTransformInjection
                s <init> (Lcom/mojang/math/Vector3f;Lcom/mojang/math/Vector3f;Lcom/mojang/math/Vector3f;)V
                i <init> (Lcom/mojang/math/Vector3f;Lcom/mojang/math/Vector3f;Lcom/mojang/math/Vector3f;Lcom/mojang/math/Vector3f;)V
            c net/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder xyz/bluspring/kilt/injections/entity/AttributeSupplierBuilderInjection
                s <init> ()V
                i <init> (Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;)V
            c net/minecraft/world/level/block/LiquidBlock xyz/bluspring/kilt/injections/world/level/block/LiquidBlockInjection
                s <init> (Lnet/minecraft/world/level/material/FlowingFluid;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V
                i <init> (Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V
            c net/minecraft/world/item/BucketItem xyz/bluspring/kilt/injections/item/BucketItemInjection
                s <init> (Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/world/item/Item$Properties;)V
                i <init> (Ljava/util/function/Supplier;Lnet/minecraft/world/item/Item$Properties;)V
            c net/minecraft/world/item/CreativeModeTab xyz/bluspring/kilt/injections/world/item/CreativeModeTabInjection
                s <init> (ILjava/lang/String;)V
                i <init> (Ljava/lang/String;)V
            c net/minecraft/world/level/block/PoweredRailBlock xyz/bluspring/kilt/injections/world/level/block/PoweredRailBlockInjection
                s <init> (Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V
                i <init> (Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Z)V
             */

            // ItemTransform
            run {
                val itemTransform = namespaced("net/minecraft/class_804", "net/minecraft/client/renderer/block/model/ItemTransform")
                val itemTransformInject = "xyz/bluspring/kilt/injections/client/render/block/model/ItemTransformInjection"

                ClassTinkerers.addTransformation(itemTransform) {
                    val vector3f = namespaced("net/minecraft/class_1160", "com/mojang/math/Vector3f")

                    run {
                        val initializer = it.visitMethod(
                            Opcodes.ACC_PUBLIC,
                            "<init>",
                            "(L$vector3f;L$vector3f;L$vector3f;L$vector3f;)V",
                            null, null
                        )

                        initializer.visitCode()

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()
                        val label3 = Label()

                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 2)
                        initializer.visitVarInsn(Opcodes.ALOAD, 3)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, itemTransform, "<init>", "(L$vector3f;L$vector3f;L$vector3f;)V", false)

                        initializer.visitLabel(label1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitTypeInsn(Opcodes.CHECKCAST, itemTransformInject)
                        initializer.visitVarInsn(Opcodes.ALOAD, 4)
                        initializer.visitMethodInsn(Opcodes.INVOKEINTERFACE, itemTransformInject, "setRightRotation", "(L$vector3f;)V", true)

                        initializer.visitLabel(label2)
                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label3)
                        initializer.visitLocalVariable("this", "L$itemTransform;", null, label0, label3, 0)
                        initializer.visitLocalVariable("vector3f", "L$vector3f;", null, label0, label3, 1)
                        initializer.visitLocalVariable("vector3f2", "L$vector3f;", null, label0, label3, 2)
                        initializer.visitLocalVariable("vector3f3", "L$vector3f;", null, label0, label3, 3)
                        initializer.visitLocalVariable("rightRotation", "L$vector3f;", null, label0, label3, 4)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // AttributeSupplier$Builder
            run {
                val attributeSupplierBuilder = namespaced("net/minecraft/class_5132\$class_5133", "net/minecraft/world/entity/ai/attributes/AttributeSupplier\$Builder")
                val attributeSupplier = namespaced("net/minecraft/class_5132", "net/minecraft/world/entity/ai/attributes/AttributeSupplier")
                ClassTinkerers.addTransformation(attributeSupplierBuilder) {
                    run {
                        val initializer = it.visitMethod(
                            Opcodes.ACC_PUBLIC, "<init>", "(L$attributeSupplier;)V",
                            null, null
                        )

                        initializer.visitCode()

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()

                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, attributeSupplierBuilder, "<init>", "()V", false)

                        initializer.visitLabel(label1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitFieldInsn(Opcodes.GETFIELD, attributeSupplierBuilder, namespaced("field_23714", "builder"), "Ljava/util/Map;")
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        initializer.visitTypeInsn(Opcodes.CHECKCAST, "xyz/bluspring/kilt/mixin/AttributeSupplierAccessor")
                        initializer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "xyz/bluspring/kilt/mixin/AttributeSupplierAccessor", "getInstances", "()Ljava/util/Map;", true)
                        initializer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true)

                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label2)
                        initializer.visitLocalVariable("this", "L$attributeSupplierBuilder;", null, label0, label2, 0)
                        initializer.visitLocalVariable("attributeMap", "L$attributeSupplier;", null, label0, label2, 1)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // LiquidBlock
            run {
                val liquidBlock = namespaced("net/minecraft/class_2404", "net/minecraft/world/level/block/LiquidBlock")
                val blockBehaviourProperties = namespaced("net/minecraft/class_4970\$class_2251", "net/minecraft/world/level/block/state/BlockBehaviour\$Properties")
                val flowingFluid = namespaced("net/minecraft/class_3609", "net/minecraft/world/level/material/FlowingFluid")
                val block = namespaced("net/minecraft/class_2248", "net/minecraft/world/level/block/Block")
                val stateDefinition = namespaced("net/minecraft/class_2689", "net/minecraft/world/level/block/state/StateDefinition")

                ClassTinkerers.addTransformation(liquidBlock) {
                    run {
                        val initializer = it.visitMethod(
                            Opcodes.ACC_PUBLIC, "<init>", "(Ljava/util/function/Supplier;L$blockBehaviourProperties;)V",
                            "(Ljava/util/function/Supplier<+L$flowingFluid;>;L$blockBehaviourProperties;)V", null
                        )

                        // this(liquidSupplier.get(), properties)
                        initializer.visitCode()

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()
                        val label3 = Label()
                        val label4 = Label()
                        val label5 = Label()
                        val label6 = Label()

                        // super(properties);
                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 2)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, block, "<init>", "(L$blockBehaviourProperties;)V", false)

                        // this.field = null;
                        initializer.visitLabel(label1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitInsn(Opcodes.ACONST_NULL)
                        initializer.visitFieldInsn(Opcodes.PUTFIELD, liquidBlock, namespaced("field_11279", "fluid"), "L$flowingFluid;")

                        initializer.visitLabel(label2)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;", false)
                        initializer.visitFieldInsn(Opcodes.PUTFIELD, liquidBlock, namespaced("field_11276", "stateCache"), "Ljava/util/List;")

                        // this.registerDefaultState(this.stateDefinition.any().setValue(FlowingFluid.LEVEL, 0));
                        initializer.visitLabel(label3)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitFieldInsn(Opcodes.GETFIELD, liquidBlock, namespaced("field_10647", "stateDefinition"), "L$stateDefinition;")

                        val stateHolder = namespaced("net/minecraft/class_2688", "net/minecraft/world/level/block/state/StateHolder")
                        val blockState = namespaced("net/minecraft/class_2680", "net/minecraft/world/level/block/state/BlockState")
                        val integerProperty = namespaced("net/minecraft/class_2758", "net/minecraft/world/level/block/state/properties/IntegerProperty")
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stateDefinition, namespaced("method_11664", "any"), "()L$stateHolder;", false)
                        initializer.visitTypeInsn(Opcodes.CHECKCAST, blockState)
                        initializer.visitFieldInsn(Opcodes.GETSTATIC, flowingFluid, namespaced("field_15900", "LEVEL"), "L$integerProperty;")

                        val property = namespaced("net/minecraft/class_2769", "net/minecraft/world/level/block/state/properties/Property")
                        initializer.visitInsn(Opcodes.ICONST_0)
                        initializer.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, blockState, namespaced("method_11657", "setValue"), "(L$property;Ljava/lang/Comparable;)Ljava/lang/Object;", false)
                        initializer.visitTypeInsn(Opcodes.CHECKCAST, blockState)
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, liquidBlock, namespaced("method_9590", "registerDefaultState"), "(L$blockState;)V", false)

                        // this.supplier = supplier;
                        initializer.visitLabel(label4)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        // by all means, this doesn't exist in LiquidBlock.
                        // but because of mixin, we're making it exist.
                        initializer.visitFieldInsn(Opcodes.PUTFIELD, liquidBlock, "supplier", "Ljava/util/function/Supplier;")

                        initializer.visitLabel(label5)
                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label6)
                        initializer.visitLocalVariable("this", "L$liquidBlock;", null, label0, label6, 0)
                        initializer.visitLocalVariable("fluidSupplier", "Ljava/util/function/Supplier;", "Ljava/util/function/Supplier<+L$flowingFluid;>;", label0, label6, 1)
                        initializer.visitLocalVariable("properties", "L$blockBehaviourProperties;", null, label0, label6, 2)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // BucketItem
            run {
                val bucketItem = namespaced("net/minecraft/class_1755", "net/minecraft/world/item/BucketItem")
                val fluid = namespaced("net/minecraft/class_3611", "net/minecraft/world/level/material/Fluid")
                val contentField = namespaced("field_7905", "content")
                val item = namespaced("net/minecraft/class_1792", "net/minecraft/world/item/Item")
                val itemProperties = namespaced("net/minecraft/class_1792\$class_1793", "net/minecraft/world/item/Item\$Properties")

                ClassTinkerers.addTransformation(bucketItem) {
                    run {
                        val initializer = it.visitMethod(
                            Opcodes.ACC_PUBLIC, "<init>",
                            "(Ljava/util/function/Supplier;L$itemProperties;)V",
                            null, null
                        )

                        initializer.visitCode();

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()
                        val label3 = Label()
                        val label4 = Label()

                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 2)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, item, "<init>", "(L$itemProperties;)V", false)

                        initializer.visitLabel(label1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bucketItem, "kilt\$setFluidSupplier", "(Ljava/util/function/Supplier;)V", false)

                        initializer.visitLabel(label2)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitInsn(Opcodes.ACONST_NULL)
                        initializer.visitFieldInsn(Opcodes.PUTFIELD, bucketItem, contentField, "L$fluid;")

                        initializer.visitLabel(label3)
                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label4)
                        initializer.visitLocalVariable("this", "L$bucketItem;", null, label0, label4, 0)
                        initializer.visitLocalVariable("fluidSupplier", "Ljava/util/function/Supplier;", null, label0, label4, 1)
                        initializer.visitLocalVariable("properties", "L$itemProperties;", null, label0, label4, 2)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // MobBucketItem
            run {
                val mobBucketItem = namespaced("net/minecraft/class_1785", "net/minecraft/world/item/MobBucketItem")
                val bucketItem = namespaced("net/minecraft/class_1755", "net/minecraft/world/item/BucketItem")
                val itemProperties = namespaced("net/minecraft/class_1792\$class_1793", "net/minecraft/world/item/Item\$Properties")

                ClassTinkerers.addTransformation(mobBucketItem) {
                    // <init>(java.util.function.Supplier<? extends EntityType<?>> entitySupplier, java.util.function.Supplier<? extends Fluid> fluidSupplier, java.util.function.Supplier<? extends SoundEvent> soundSupplier, Item.Properties properties)V
                    run {
                        val initializer = it.visitMethod(
                            Opcodes.ACC_PUBLIC, "<init>",
                            "(Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/Supplier;L$itemProperties;)V",
                            null, null
                        )

                        initializer.visitCode();

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()
                        val label3 = Label()
                        val label4 = Label()

                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 2)
                        initializer.visitVarInsn(Opcodes.ALOAD, 4)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, bucketItem, "<init>", "(Ljava/util/function/Supplier;L$itemProperties;)V", false)

                        initializer.visitLabel(label1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mobBucketItem, "setEntityTypeSupplier", "(Ljava/util/function/Supplier;)V", false)

                        initializer.visitLabel(label2)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 2)
                        initializer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mobBucketItem, "setEmptySoundSupplier", "(Ljava/util/function/Supplier;)V", false)

                        initializer.visitLabel(label3)
                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label4)
                        initializer.visitLocalVariable("this", "L$mobBucketItem;", null, label0, label4, 0)
                        initializer.visitLocalVariable("entitySupplier", "Ljava/util/function/Supplier;", null, label0, label4, 1)
                        initializer.visitLocalVariable("fluidSupplier", "Ljava/util/function/Supplier;", null, label0, label4, 2)
                        initializer.visitLocalVariable("soundSupplier", "Ljava/util/function/Supplier;", null, label0, label4, 3)
                        initializer.visitLocalVariable("properties", "L$itemProperties;", null, label0, label4, 4)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // CreativeModeTab
            run {
                val creativeModeTab = namespaced("net/minecraft/class_1761", "net/minecraft/world/item/CreativeModeTab")

                ClassTinkerers.addTransformation(creativeModeTab) {
                    run {
                        val initializer = it.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null)

                        initializer.visitCode()

                        val label0 = Label()
                        val label1 = Label()
                        val label2 = Label()

                        initializer.visitLabel(label0)
                        initializer.visitVarInsn(Opcodes.ALOAD, 0)
                        initializer.visitInsn(Opcodes.ICONST_M1)
                        initializer.visitVarInsn(Opcodes.ALOAD, 1)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, creativeModeTab, "<init>", "(ILjava/lang/String;)V", false)

                        initializer.visitLabel(label1)
                        initializer.visitInsn(Opcodes.RETURN)

                        initializer.visitLabel(label2)
                        initializer.visitLocalVariable("this", "L$creativeModeTab;", null, label0, label2, 0)
                        initializer.visitLocalVariable("string", "Ljava/lang/String;", null, label0, label2, 1)

                        initializer.visitMaxs(0, 0)
                        initializer.visitEnd()
                    }
                }
            }

            // PoweredRailBlock
            run {
                val poweredRailBlock = namespaced("net/minecraft/class_2442", "net/minecraft/world/level/block/PoweredRailBlock")
                val blockBehaviourProperties = namespaced("net/minecraft/class_4970\$class_2251", "net/minecraft/world/level/block/state/BlockBehaviour\$Properties")
                val poweredRailBlockInjection = "xyz/bluspring/kilt/injections/world/level/block/PoweredRailBlockInjection"

                ClassTinkerers.addTransformation(poweredRailBlock) {
                    val initializer = it.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L$blockBehaviourProperties;Z)V", null, null)

                    initializer.visitCode()

                    val label0 = Label()
                    val label1 = Label()
                    val label2 = Label()
                    val label3 = Label()

                    initializer.visitLabel(label0)
                    initializer.visitVarInsn(Opcodes.ALOAD, 0)
                    initializer.visitVarInsn(Opcodes.ALOAD, 1)
                    initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, poweredRailBlock, "<init>", "(L$blockBehaviourProperties;)V", false)

                    initializer.visitLabel(label1)
                    initializer.visitVarInsn(Opcodes.ALOAD, 0)
                    initializer.visitTypeInsn(Opcodes.CHECKCAST, poweredRailBlockInjection)
                    initializer.visitVarInsn(Opcodes.ILOAD, 2)
                    initializer.visitMethodInsn(Opcodes.INVOKEINTERFACE, poweredRailBlockInjection, "kilt\$setActivator", "(Z)V", true)

                    initializer.visitLabel(label2)
                    initializer.visitInsn(Opcodes.RETURN)

                    initializer.visitLabel(label3)
                    initializer.visitLocalVariable("this", "L$poweredRailBlock;", null, label0, label3, 0)
                    initializer.visitLocalVariable("properties", "L$blockBehaviourProperties;", null, label0, label3, 1)
                    initializer.visitLocalVariable("isPoweredRail", "Z", null, label0, label3, 2)

                    initializer.visitMaxs(0, 0)
                    initializer.visitEnd()
                }
            }
        }

        // Preload here in order to load access transformers
        Kilt.loader.preloadMods()

        AccessTransformerLoader.runTransformers()
    }

    private fun namespaced(intermediary: String, mojmapped: String): String {
        return if (namespace == "intermediary")
            intermediary
        else
            mojmapped
    }
}