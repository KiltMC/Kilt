package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.ObjectHolderDefinalizer
import xyz.bluspring.kilt.loader.remap.fixers.AnnotationWorkaroundFixer
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentRemapper
import xyz.bluspring.kilt.loader.remap.fixers.EventClassVisibilityFixer
import xyz.bluspring.kilt.loader.remap.fixers.EventEmptyInitializerFixer
import xyz.bluspring.kilt.util.KiltHelper
import java.lang.reflect.Modifier
import java.net.URL

class KiltEarlyRiser : Runnable {
    override fun run() {
        processForgeClasses()

        val mappingResolver = FabricLoader.getInstance().mappingResolver
        val namespace = "intermediary"

        // EnchantmentCategory has an abstract method that doesn't exactly play nicely with enum extension.
        // So, we need to modify it to have a method body that works with the Forge API.
        run {
            val enchCategoryIm = "net.minecraft.class_1886"
            val itemIm = "net.minecraft.class_1792"
            val enchantmentCategory = mappingResolver.mapClassName(namespace, enchCategoryIm)

            ClassTinkerers.addTransformation(enchantmentCategory) { classNode ->
                classNode.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_ENUM

                run {
                    val canEnchantName = mappingResolver.mapMethodName(namespace, enchCategoryIm, "method_8177", "(L${itemIm.replace(".", "/")};)Z")
                    // remove it first
                    classNode.methods.removeIf { it.name == canEnchantName }

                    val item = mappingResolver.mapClassName(namespace, itemIm)
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
            val bakedModelIm = "net.minecraft.class_1087"
            val itemTransformsIm = "net.minecraft.class_809"
            val bakedModel = mappingResolver.mapClassName(namespace, bakedModelIm)

            ClassTinkerers.addTransformation(bakedModel) { classNode ->
                run {
                    val getTransformsName = mappingResolver.mapMethodName(namespace, bakedModelIm, "method_4709", "()L${itemTransformsIm.replace(".", "/")};")

                    classNode.methods.removeIf { it.name == getTransformsName }

                    val itemTransforms = mappingResolver.mapClassName(namespace, itemTransformsIm).replace(".", "/")
                    val noTransforms = mappingResolver.mapFieldName(namespace, itemTransformsIm, "field_4301", "L${itemTransformsIm.replace(".", "/")};")

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
                val mobBucketItem = mappingResolver.mapClassName(namespace, "net.minecraft.class_1785").replace(".", "/")
                val bucketItem = mappingResolver.mapClassName(namespace, "net.minecraft.class_1755").replace(".", "/")
                val itemProperties = mappingResolver.mapClassName(namespace, "net.minecraft.class_1792\$class_1793").replace(".", "/")

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
                        initializer.visitVarInsn(Opcodes.ALOAD, 3)
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
                val flowingFluidIm = "net.minecraft.class_3609"
                val liquidBlockIm = "net.minecraft.class_2404"
                val flowingFluid = mappingResolver.mapClassName(namespace, flowingFluidIm).replace(".", "/")
                val liquidBlock = mappingResolver.mapClassName(namespace, liquidBlockIm).replace(".", "/")

                ClassTinkerers.addTransformation(liquidBlock) {
                    it.methods.forEach { methodNode ->
                        if (methodNode.name.startsWith("<") || Modifier.isStatic(methodNode.access) || Modifier.isAbstract(methodNode.access))
                            return@forEach

                        if (methodNode.instructions.none { a -> a is FieldInsnNode && a.name == mappingResolver.mapFieldName(namespace, liquidBlockIm, "field_11279", "L${flowingFluidIm.replace(".", "/")};") })
                            return@forEach

                        methodNode.instructions.insertBefore(methodNode.instructions.first, InsnList().apply {
                            this.add(VarInsnNode(Opcodes.ALOAD, 0))
                            this.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, liquidBlock, "getFluid", "()L$flowingFluid;"))
                        })
                    }
                }
            }
        }

        run {
            val biomeSpecialEffectsMapped =
                KiltRemapper.remapClass("net/minecraft/world/level/biome/BiomeSpecialEffects")
            val grassColorModifierMapped =
                KiltRemapper.remapClass("net/minecraft/world/level/biome/BiomeSpecialEffects\$GrassColorModifier")
            val biomeInjectionName =
                "xyz/bluspring/kilt/injections/world/biome/BiomeSpecialEffectsGrassColorModifierInjection"
            val colorModifierName = "$grassColorModifierMapped\$ColorModifier"
            val modifyColor = mappingResolver.mapMethodName(
                "intermediary",
                "net.minecraft.class_4763\$class_5486",
                "method_30823",
                "(DDI)I"
            )

            val classWriter = ClassWriter(Opcodes.ASM9)
            classWriter.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT,
                colorModifierName,
                null,
                "java/lang/Object",
                arrayOf("$biomeInjectionName\$ColorModifier")
            )

            classWriter.visitNestHost(biomeSpecialEffectsMapped)
            classWriter.visitAnnotation("Ljava/lang/FunctionalInterface;", true)
            classWriter.visitInnerClass(
                grassColorModifierMapped,
                biomeSpecialEffectsMapped,
                grassColorModifierMapped.removePrefix(biomeSpecialEffectsMapped).removePrefix("\$"),
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_ENUM
            )
            classWriter.visitInnerClass(
                colorModifierName,
                grassColorModifierMapped,
                "ColorModifier",
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT
            )
            classWriter.visitInnerClass(
                "$biomeInjectionName\$ColorModifier",
                biomeInjectionName,
                "ColorModifier",
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT
            )

            classWriter.visitMethod(
                Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT,
                "modifyGrassColor",
                "(DDI)I",
                null,
                null
            )
            classWriter.visitEnd()

            ClassTinkerers.define("$grassColorModifierMapped\$ColorModifier", classWriter.toByteArray())

            ClassTinkerers.addTransformation(grassColorModifierMapped) { classNode ->
                classNode.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_ENUM // why the fuck is this needed????
                classNode.visitInnerClass(colorModifierName, grassColorModifierMapped, "ColorModifier", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT)

                // Need to create this, and make sure it remaps to the pre-existing one.
                /*
                Expected code:
                public static GrassColorModifier create(String name, String id, ColorModifier delegate) {
                    return GrassColorModifier.create(name, id, (BiomeSpecialEffectsGrassColorModifierInjection.ColorModifier) delegate);
                }
                 */
                classNode.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "create", "(Ljava/lang/String;Ljava/lang/String;L$colorModifierName;)L$grassColorModifierMapped;", null, null).apply {
                    this.visitCode()

                    val label0 = Label()
                    val label1 = Label()
                    val label2 = Label()

                    this.visitLabel(label0)
                    this.visitVarInsn(Opcodes.ALOAD, 0)
                    this.visitVarInsn(Opcodes.ALOAD, 1)
                    this.visitVarInsn(Opcodes.ALOAD, 2)
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, grassColorModifierMapped, "create", "(Ljava/lang/String;Ljava/lang/String;L$biomeInjectionName\$ColorModifier;)L$grassColorModifierMapped;", false)

                    this.visitLabel(label1)
                    this.visitInsn(Opcodes.ARETURN)

                    this.visitLabel(label2)
                    this.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label2, 0)
                    this.visitLocalVariable("id", "Ljava/lang/String;", null, label0, label2, 1)
                    this.visitLocalVariable("delegate", "L$biomeInjectionName\$ColorModifier;", null, label0, label2, 2)

                    this.visitMaxs(0, 0)
                    this.visitEnd()
                }

                // Use a delegate for modifyColor
                // Expected code:
                /*
                public int modifyColor(double x, double z, int grassColor) {
                    return this.kilt$getDelegate().modifyGrassColor(x, z, grassColor);
                }
                 */
                run {
                    val originalModifyColorMethod = classNode.methods.first { it.name == modifyColor }
                    classNode.methods.removeIf { it.name == modifyColor }

                    val modifyColorMethod = classNode.visitMethod(Opcodes.ACC_PUBLIC, originalModifyColorMethod.name, originalModifyColorMethod.desc, originalModifyColorMethod.signature, originalModifyColorMethod.exceptions.toTypedArray())

                    modifyColorMethod.visitCode()

                    val label0 = Label()
                    val label1 = Label()

                    modifyColorMethod.visitLabel(label0)
                    modifyColorMethod.visitVarInsn(Opcodes.ALOAD, 0)
                    modifyColorMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, grassColorModifierMapped, "kilt\$getDelegate", "()L$biomeInjectionName\$ColorModifier;", false)
                    modifyColorMethod.visitVarInsn(Opcodes.DLOAD, 1)
                    modifyColorMethod.visitVarInsn(Opcodes.DLOAD, 3)
                    modifyColorMethod.visitVarInsn(Opcodes.ILOAD, 5)
                    modifyColorMethod.visitMethodInsn(Opcodes.INVOKEINTERFACE, "$biomeInjectionName\$ColorModifier", "modifyGrassColor", "(DDI)I", true)
                    modifyColorMethod.visitInsn(Opcodes.IRETURN)

                    modifyColorMethod.visitLabel(label1)
                    modifyColorMethod.visitLocalVariable("this", "L$grassColorModifierMapped;", null, label0, label1, 0)
                    modifyColorMethod.visitLocalVariable("x", "D", null, label0, label1, 1)
                    modifyColorMethod.visitLocalVariable("z", "D", null, label0, label1, 3)
                    modifyColorMethod.visitLocalVariable("grassColor", "I", null, label0, label1, 5)

                    modifyColorMethod.visitMaxs(6, 6)
                    modifyColorMethod.visitEnd()
                }
            }

            ClassTinkerers.addTransformation(biomeSpecialEffectsMapped) { classNode ->
                classNode.visitNestMember(colorModifierName)
                classNode.visitInnerClass(colorModifierName, grassColorModifierMapped, "ColorModifier", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT)
            }
        }

        run {
            ClassTinkerers.addTransformation("joptsimple.internal.Strings") { classNode ->
                // What the fuck, why
                if (classNode.methods.none { it.name == "join" && it.desc == "([Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;" }) {
                    val joinMethod = classNode.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "join", "([Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null)
                    joinMethod.visitCode()
                    val label0 = Label()
                    val label1 = Label()

                    joinMethod.visitLabel(label0)
                    joinMethod.visitFieldInsn(Opcodes.GETSTATIC, "xyz/bluspring/kilt/util/KiltHelper", "INSTANCE", "Lxyz/bluspring/kilt/util/KiltHelper;")
                    joinMethod.visitVarInsn(Opcodes.ALOAD, 0)
                    joinMethod.visitVarInsn(Opcodes.ALOAD, 1)
                    joinMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "xyz/bluspring/kilt/util/KiltHelper", "joinToString", "([Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false)
                    joinMethod.visitInsn(Opcodes.ARETURN)

                    joinMethod.visitLabel(label1)
                    joinMethod.visitLocalVariable("pieces", "[Ljava/lang/String;", null, label0, label1, 0)
                    joinMethod.visitLocalVariable("separator", "Ljava/lang/String;", null, label0, label1, 1)
                    joinMethod.visitMaxs(3, 2)
                    joinMethod.visitEnd()
                }
            }
        }

        // Forcefully load all classes under EventBus that have been modified by Kilt's fork.
        // This is to work around an issue with CreativeCore where their loaded EventBus overrides Kilt's.
        run {
            val eventBusPackage = "net.minecraftforge.eventbus"
            val eventBusSlashed = eventBusPackage.replace(".", "/")
            val modifiedEventBusClasses = listOf(
                "IEventListenerFactory",
                "BusBuilderImpl"
            )

            val helperUrl = Kilt::class.java.classLoader.getResource("$eventBusSlashed/KiltHelper.class")!!

            for (className in modifiedEventBusClasses) {
                ClassTinkerers.addReplacement("$eventBusPackage.$className") { classNode ->
                    // Reset everything to a blank state, because fuck that
                    classNode.fields?.clear()
                    classNode.methods?.clear()
                    classNode.visibleAnnotations?.clear()
                    classNode.invisibleAnnotations?.clear()
                    classNode.visibleTypeAnnotations?.clear()
                    classNode.invisibleTypeAnnotations?.clear()

                    val url = URL(
                        helperUrl.protocol, helperUrl.host, helperUrl.port, helperUrl.file
                            .replace("$eventBusSlashed/KiltHelper", "$eventBusSlashed/${className.replace(".", "/")}")
                    )
                    val classReader = ClassReader(url.readBytes())
                    classReader.accept(classNode, 0)
                }
            }
        }
    }

    private val ignoredKeywords = listOf("kilt", "fml", "mixin", "modlauncher", "kotlinforforge", "architectury")

    // Required as Forge runs itself through ASM to fix events and ObjectHolders and such using ModLauncher.
    // So annoying.
    private fun processForgeClasses() {
        val classes = KiltHelper.getForgeClassNodes()

        classes.forEach { classNode ->
            if (ignoredKeywords.any { classNode.name.lowercase().contains(it) })
                return@forEach

            if (classNode.name.contains("ForgeConfigSpec") || classNode.outerClass?.contains("ForgeConfigSpec") == true || classNode.name.lowercase().contains("coremod"))
                return@forEach

            ClassTinkerers.addTransformation(classNode.name) {
                EventClassVisibilityFixer.fixClass(it)
                EventEmptyInitializerFixer.fixClass(it, classes)
                ObjectHolderDefinalizer.processClass(it)
                AnnotationWorkaroundFixer.fixClass(it)
                EnvironmentRemapper.remapClass(it)
            }
        }
    }
}