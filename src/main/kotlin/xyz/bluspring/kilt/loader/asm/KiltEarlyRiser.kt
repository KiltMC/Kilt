package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.fixers.EventClassVisibilityFixer
import xyz.bluspring.kilt.loader.fixers.EventEmptyInitializerFixer
import xyz.bluspring.kilt.loader.mixin.KiltMixinLoader
import xyz.bluspring.kilt.loader.remap.ObjectHolderDefinalizer
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.reflect.Modifier

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

        // i haven't created a way to replace the abstracted methods yet, so this will do.
        run {
            val bakedModel = namespaced("net.minecraft.class_1087", "net.minecraft.client.resources.model.BakedModel")

            ClassTinkerers.addTransformation(bakedModel) { classNode ->
                run {
                    val getTransformsName = namespaced("method_4709", "getTransforms")

                    classNode.methods.removeIf { it.name == getTransformsName }

                    val itemTransforms = namespaced("net/minecraft/class_809", "net/minecraft/client/renderer/block/model/ItemTransforms")
                    val noTransforms = namespaced("field_4301", "NO_TRANSFORMS")

                    // this method should look like this
                    /*
                    default ItemTransforms getTransforms() {
                        return ItemTransforms.NO_TRANSFORMS;
                    }
                     */

                    val getTransform = classNode.visitMethod(Opcodes.ACC_PUBLIC, getTransformsName, "()L$itemTransforms;", null, null)
                    getTransform.visitCode()

                    val label0 = Label()
                    val label1 = Label()

                    getTransform.visitLabel(label0)
                    getTransform.visitFieldInsn(Opcodes.GETSTATIC, itemTransforms, noTransforms, "L$itemTransforms;")
                    getTransform.visitInsn(Opcodes.ARETURN)

                    getTransform.visitLabel(label1)
                    getTransform.visitLocalVariable("this", "L${bakedModel.replace(".", "/")};", null, label0, label1, 0)
                    getTransform.visitMaxs(1, 1)
                    getTransform.visitEnd()
                }
            }
        }

        // We need to add some new initializers because thanks Forge.
        // I probably should've done this from the beginning, honestly.

        // Most initializers/constructors are now created using @CreateInitializer,
        // but some are still made here because it's a bit harder to implement super() calls
        // for a class that also has its own mixin-created initializer.
        run {
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

        // BucketItem and LiquidBlock require special treatment as there is currently a weird issue
        // where @Inject doesn't actually properly allow for injecting into multiple targets.
        // TODO: Remove this when that bug is fixed
        run {
            run {
                val flowingFluid = namespaced("net/minecraft/class_3609", "net/minecraft/world/level/material/FlowingFluid")
                val liquidBlock = namespaced("net/minecraft/class_2404", "net/minecraft/world/level/block/LiquidBlock")

                ClassTinkerers.addTransformation(liquidBlock) {
                    it.methods.forEach { methodNode ->
                        if (methodNode.name.startsWith("<") || Modifier.isStatic(methodNode.access) || Modifier.isAbstract(methodNode.access))
                            return@forEach

                        if (methodNode.instructions.none { a -> a is FieldInsnNode && a.name == namespaced("field_11279", "fluid") })
                            return@forEach

                        methodNode.instructions.insertBefore(methodNode.instructions.first, InsnList().apply {
                            this.add(VarInsnNode(Opcodes.ALOAD, 0))
                            this.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, liquidBlock, "getFluid", "()L$flowingFluid;"))
                        })
                    }
                }
            }

            run {
                val fluid = namespaced("net/minecraft/class_3611", "net/minecraft/world/level/material/Fluid")
                val bucketItem = namespaced("net/minecraft/class_2404", "net/minecraft/world/item/BucketItem")

                ClassTinkerers.addTransformation(bucketItem) {
                    it.methods.forEach { methodNode ->
                        if (methodNode.name.startsWith("<") || Modifier.isStatic(methodNode.access) || Modifier.isAbstract(methodNode.access))
                            return@forEach

                        if (methodNode.instructions.none { a -> a is FieldInsnNode && a.name == namespaced("field_7905", "content") })
                            return@forEach

                        methodNode.instructions.insertBefore(methodNode.instructions.first, InsnList().apply {
                            this.add(VarInsnNode(Opcodes.ALOAD, 0))
                            this.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, bucketItem, "getFluid", "()L$fluid;"))
                        })
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
                EventClassVisibilityFixer.fixClass(it)
                EventEmptyInitializerFixer.fixClass(it)
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