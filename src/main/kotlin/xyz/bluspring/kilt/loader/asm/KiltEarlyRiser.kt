package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mixin.KiltMixinLoader
import xyz.bluspring.kilt.loader.remap.ObjectHolderDefinalizer
import xyz.bluspring.kilt.loader.superfix.CommonSuperFixer
import xyz.bluspring.kilt.util.KiltHelper

class KiltEarlyRiser : Runnable {
    private val namespace = FabricLauncherBase.getLauncher().targetNamespace

    override fun run() {
        processForgeClasses()

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
        // I probably should've done this from the beginning, honestly.

        // Most initializers/constructors are now created using @CreateInitializer,
        // but some are still made here because it's a bit harder to implement super() calls
        // for a class that also has its own mixin-created initializer.
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
        }

        Kilt.loader.preloadMods()
        KiltMixinLoader.init(Kilt.loader.modLoadingQueue.stream().toList())
        AccessTransformerLoader.runTransformers()
    }

    private val ignoredKeywords = listOf("kilt", "fml", "mixin")

    // Required as Forge runs itself through ASM to fix events and ObjectHolders and such using ModLauncher.
    // So annoying.
    private fun processForgeClasses() {
        val classes = KiltHelper.getForgeClassNodes()

        classes.forEach { classNode ->
            if (ignoredKeywords.any { classNode.name.lowercase().contains(it) })
                return@forEach

            ClassTinkerers.addTransformation(classNode.name) {
                CommonSuperFixer.fixClass(it)
                ObjectHolderDefinalizer.processClass(it)
            }
        }
    }

    private fun namespaced(intermediary: String, mojmapped: String): String {
        return if (namespace == "intermediary")
            intermediary
        else
            mojmapped
    }
}