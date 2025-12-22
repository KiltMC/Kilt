package xyz.bluspring.kilt.compat.create

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

//// class version 61.0 (61)
//// access flags 0x601
//public abstract interface com/tterrag/registrate/builders/FluidBuilder$FluidTypeFactory {
//
//  // compiled from: FluidBuilder.java
//  NESTHOST com/tterrag/registrate/builders/FluidBuilder
//
//  @Ljava/lang/FunctionalInterface;()
//  // access flags 0x609
//  public static abstract INNERCLASS com/tterrag/registrate/builders/FluidBuilder$FluidTypeFactory com/tterrag/registrate/builders/FluidBuilder FluidTypeFactory
//  // access flags 0x19
//  public final static INNERCLASS net/minecraftforge/fluids/FluidType$Properties net/minecraftforge/fluids/FluidType Properties
//
//  // access flags 0x401
//  public abstract create(Lnet/minecraftforge/fluids/FluidType$Properties;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraftforge/fluids/FluidType;
//}

class CompatEarlyRiser : Runnable {
    override fun run() {
        val fluidBuilder = "com/tterrag/registrate/builders/FluidBuilder"
        val fluidTypeFactoryInterface = "com/tterrag/registrate/builders/FluidBuilder\$FluidTypeFactory"

        val mappingResolver = FabricLoader.getInstance().mappingResolver
        val resourceLocation = mappingResolver.mapClassName("intermediary", "net.minecraft.class_2960").replace(".", "/")

        val fluidTypeProperties = "net/minecraftforge/fluids/FluidType\$Properties"
        val fluidType = "net/minecraftforge/fluids/FluidType"

        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

        writer.visit(
            Opcodes.V17,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT or Opcodes.ACC_INTERFACE,
            fluidTypeFactoryInterface,
            null, "java/lang/Object", null
        )

        writer.visitNestHost(fluidBuilder)

        writer.visitAnnotation("Ljava/lang/FunctionalInterface;", true)

        writer.visitInnerClass(
            fluidTypeFactoryInterface,
            fluidBuilder,
            "FluidTypeFactory",
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_ABSTRACT or Opcodes.ACC_INTERFACE
        )

        writer.visitInnerClass(
            fluidTypeProperties,
            fluidType,
            "Properties",
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL
        )

        writer.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT,
            "create",
            //"(Lnet/minecraftforge/fluids/FluidType\$Properties;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraftforge/fluids/FluidType;",
            "(L$fluidTypeProperties;L$resourceLocation;L$resourceLocation;)L$fluidType;",
            null, null
        )
        ClassTinkerers.define(fluidTypeFactoryInterface, writer.toByteArray())
    }
}