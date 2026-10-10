package xyz.bluspring.kilt.loader.asm

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.*
import xyz.bluspring.fork.mm.api.ClassTinkerers
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.remap.fixers.AnnotationWorkaroundFixer
import xyz.bluspring.kilt.loader.remap.fixers.EnvironmentRemapper
import xyz.bluspring.kilt.loader.remap.fixers.EventClassVisibilityFixer
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

        run {
            val savedDataType = "net/minecraft/world/level/saveddata/SavedDataType"
            val factoryName = $$"$$savedDataType$Factory"
            val factoryInjectionName = $$"xyz/bluspring/kilt/injections/world/level/saveddata/SavedDataTypeInjection$Factory"

            run {
                val classWriter = ClassWriter(Opcodes.ASM9)
                classWriter.visit(
                    Opcodes.V25,
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT,
                    factoryName,
                    "L$factoryName;<TT;>",
                    "Ljava/lang/Object;",
                    arrayOf(factoryInjectionName)
                )

                classWriter.visitNestHost(savedDataType)
                classWriter.visitAnnotation("Ljava/lang/FunctionalInterface;", true)
                classWriter.visitInnerClass(
                    factoryName,
                    savedDataType,
                    factoryName.removePrefix(savedDataType).removePrefix("$"),
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT
                )

                classWriter.visitMethod(
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT,
                    "create",
                    "(Lnet/minecraft/server/level/ServerLevel;)Ljava/lang/Object;",
                    "(Lnet/minecraft/server/level/ServerLevel;)TT;",
                    null
                )
                classWriter.visitEnd()

                ClassTinkerers.define(factoryName, classWriter.toByteArray())
            }

            run {
                ClassTinkerers.addTransformation(savedDataType) { classNode ->
                    val matchingMethods = classNode.methods.filter { it.desc.contains(factoryInjectionName) }

                    for (methodNode in matchingMethods) {
                        val newMethod = classNode.visitMethod(methodNode.access, methodNode.name,
                            methodNode.desc.replace(factoryInjectionName, factoryName),
                            methodNode.signature?.replace(factoryInjectionName, factoryName),
                            methodNode.exceptions?.toTypedArray()
                        )

                        newMethod.visitCode()

                        val l0 = Label()
                        val l1 = Label()

                        newMethod.visitLabel(l0)
                        val varOffset = if (Modifier.isStatic(methodNode.access)) 0 else 1

                        if (varOffset == 1)
                            newMethod.visitVarInsn(Opcodes.ALOAD, 0)

                        val descriptor = Type.getArgumentTypes(methodNode.desc)
                        val signature = methodNode.signature?.let { KiltHelper.splitSignature(descriptor.size, it) }

                        for ((index, type) in descriptor.withIndex()) {
                            newMethod.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index + varOffset)
                        }

                        newMethod.visitMethodInsn(if (Modifier.isStatic(methodNode.access)) Opcodes.INVOKESTATIC else Opcodes.INVOKEVIRTUAL, classNode.name,
                            methodNode.name, methodNode.desc, false)

                        newMethod.visitLabel(l1)

                        if (varOffset == 1)
                            newMethod.visitLocalVariable("this", "L${classNode.name};", classNode.signature, l0, l1, 0)

                        for ((index, type) in descriptor.withIndex()) {
                            newMethod.visitLocalVariable("var$index", type.descriptor.replace(factoryInjectionName, factoryName), signature?.get(index)?.replace(factoryInjectionName, factoryName), l0, l1, index + varOffset)
                        }

                        newMethod.visitMaxs(1, descriptor.size + varOffset)
                        newMethod.visitEnd()
                    }
                }
            }
        }

        run {
            val biomeSpecialEffectsMapped =
                ("net/minecraft/world/level/biome/BiomeSpecialEffects")
            val grassColorModifierMapped =
                ("net/minecraft/world/level/biome/BiomeSpecialEffects\$GrassColorModifier")
            val biomeInjectionName =
                "xyz/bluspring/kilt/injections/world/level/biome/BiomeSpecialEffectsInjection\$GrassColorModifierInjection"
            val colorModifierName = "$grassColorModifierMapped\$ColorModifier"

            val classWriter = ClassWriter(Opcodes.ASM9)
            classWriter.visit(
                Opcodes.V21,
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
                public GrassColorModifier(String name, ColorModifier delegate) {
                    this(name, (BiomeSpecialEffectsGrassColorModifierInjection.ColorModifier) delegate);
                }
                 */
                classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;L$colorModifierName;)V", null, null).apply {
                    this.visitCode()

                    val label0 = Label()
                    val label1 = Label()

                    this.visitLabel(label0)
                    this.visitVarInsn(Opcodes.ALOAD, 0)
                    this.visitVarInsn(Opcodes.ALOAD, 1)
                    this.visitVarInsn(Opcodes.ALOAD, 2)
                    this.visitMethodInsn(Opcodes.INVOKESPECIAL, grassColorModifierMapped, "<init>", "(Ljava/lang/String;L$biomeInjectionName\$ColorModifier;)V", false)
                    this.visitInsn(Opcodes.RETURN)

                    this.visitLabel(label1)
                    this.visitLocalVariable("this", "L$grassColorModifierMapped;", null, label0, label1, 0)
                    this.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label1, 1)
                    this.visitLocalVariable("delegate", "L$biomeInjectionName\$ColorModifier;", null, label0, label1, 2)

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
                /*run {
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
                }*/
            }

            ClassTinkerers.addTransformation(biomeSpecialEffectsMapped) { classNode ->
                classNode.visitNestMember(colorModifierName)
                classNode.visitInnerClass(colorModifierName, grassColorModifierMapped, "ColorModifier", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT)
            }
        }

        // Turns out some mods extend some final classes in Sodium, which are apparently not final in Rubidium/Embeddium.
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            ClassTinkerers.addTransformation("me.jellysquid.mods.sodium.client.world.WorldSlice") { classNode ->
                classNode.access = classNode.access and Opcodes.ACC_FINAL.inv()
            }
        }

        // Forcefully load the Forge config classes to override Forge Config API Port.
        run {
            val modifiedConfigClasses = listOf(
                "net.neoforged.fml.config.ConfigTracker",
                "net.neoforged.fml.config.ConfigWatcher",
                "net.neoforged.fml.config.IConfigSpec",
                "net.neoforged.fml.config.LoadedConfig",
                "net.neoforged.fml.config.ModConfig",
                "net.neoforged.fml.config.ModConfigs",
                "net.neoforged.neoforge.client.gui.ConfigurationScreen",
                "net.neoforged.neoforge.common.ModConfigSpec",
                $$"net.neoforged.neoforge.common.ModConfigSpec$BooleanValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$Builder",
                $$"net.neoforged.neoforge.common.ModConfigSpec$BuilderContext",
                $$"net.neoforged.neoforge.common.ModConfigSpec$ConfigValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$DoubleValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$EnumValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$IntValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$ListValueSpec",
                $$"net.neoforged.neoforge.common.ModConfigSpec$LongValue",
                $$"net.neoforged.neoforge.common.ModConfigSpec$RestartType",
                $$"net.neoforged.neoforge.common.ModConfigSpec$Range",
                $$"net.neoforged.neoforge.common.ModConfigSpec$ValueSpec",
                "net.neoforged.neoforge.common.TranslatableEnum",
            )

            val helperUrl = Kilt::class.java.classLoader.getResource("net/neoforged/neoforge/common/NeoForge.class")!!

            for (className in modifiedConfigClasses) {
                ClassTinkerers.addReplacement(className) { classNode ->
                    // Reset everything to a blank state, because fuck that
                    classNode.fields?.clear()
                    classNode.methods?.clear()
                    classNode.visibleAnnotations?.clear()
                    classNode.invisibleAnnotations?.clear()
                    classNode.visibleTypeAnnotations?.clear()
                    classNode.invisibleTypeAnnotations?.clear()

                    val url = URL(
                        helperUrl.protocol, helperUrl.host, helperUrl.port, helperUrl.file
                            .replace("net/neoforged/neoforge/common/NeoForge", className.replace(".", "/"))
                    )
                    val classReader = ClassReader(url.readBytes())
                    classReader.accept(classNode, 0)
                }
            }
        }
    }

    private val ignoredKeywords = listOf("kilt", "fml", "neoforgespi", "mixin", "modlauncher", "kotlinforforge", "architectury", "versions", "internal")

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
                AnnotationWorkaroundFixer.fixClass(it)
                EnvironmentRemapper.remapClass(it)
            }
        }
    }
}
