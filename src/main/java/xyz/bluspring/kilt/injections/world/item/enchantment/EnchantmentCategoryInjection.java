package xyz.bluspring.kilt.injections.world.item.enchantment;

import com.google.common.io.Files;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.mixin.EnchantmentCategoryAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public interface EnchantmentCategoryInjection {
    static EnchantmentCategory create(String name, Predicate<Item> delegate) {
        var value = EnumUtils.addEnumToClass(
            EnchantmentCategory.class, EnchantmentCategoryAccessor.getValues(),
                name, (size) -> {
                    // EnchantmentCategory is an abstract enum(?), so some strange
                    // hacks need to be done for it to work.
                    // In our case, we need to generate the classes at runtime.
                    var classNode = new ClassNode(Opcodes.ASM9);
                    var categoryType = Type.getType(EnchantmentCategory.class);

                    classNode.visit(
                            Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM | Opcodes.ACC_SUPER,
                            categoryType.getInternalName() + "$" + size,
                            null,
                            categoryType.getInternalName(),
                            null
                    );

                    // Separate these into their own blocks, so they're a bit easier to comprehend
                    {
                        // new EnchantmentCategory$index(String, int)
                        var initializer = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;I)V", null, null);
                        initializer.visitCode();

                        var label0 = new Label();
                        var label1 = new Label();

                        initializer.visitLabel(label0);
                        initializer.visitVarInsn(Opcodes.ALOAD, 0);
                        initializer.visitVarInsn(Opcodes.ALOAD, 1);
                        initializer.visitVarInsn(Opcodes.ILOAD, 2);
                        // super(string, i)
                        initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, categoryType.getInternalName(), "<init>", "(Ljava/lang/String;I)V", false);

                        initializer.visitLabel(label1);
                        initializer.visitLocalVariable("this", categoryType.getInternalName(), null, label0, label1, 0);
                        initializer.visitLocalVariable("string", "Ljava/lang/String;", null, label0, label1, 1);
                        initializer.visitLocalVariable("i", "I", null, label0, label1, 2);

                        initializer.visitMaxs(0, 0);

                        initializer.visitEnd();
                    }

                    {
                        var remapper = FabricLoader.getInstance().getMappingResolver();

                        // doing this was actually unintentionally smart, because this
                        // would get remapped
                        var itemType = Type.getType(Item.class);
                        var canEnchant = classNode.visitMethod(Opcodes.ACC_PUBLIC,
                                // This is definitely getting remapped,
                                // so let's remap it when needed.
                                remapper.mapMethodName(
                                        FabricLauncherBase.getLauncher().getTargetNamespace(),
                                        itemType.getClassName(),
                                        "method_8177",
                                        "(" + itemType.getDescriptor() + ")Z"
                                ),
                                "(" + itemType.getDescriptor() + ")Z",
                                null, null
                        );
                        var injectionType = Type.getType(EnchantmentCategoryInjection.class);

                        canEnchant.visitCode();

                        var label0 = new Label();
                        var label1 = new Label();

                        canEnchant.visitLabel(label0);

                        // ((EnchantmentCategoryInjection) (Object) this).getDelegate()
                        canEnchant.visitVarInsn(Opcodes.ALOAD, 0);
                        canEnchant.visitTypeInsn(Opcodes.CHECKCAST, injectionType.getInternalName());
                        canEnchant.visitMethodInsn(Opcodes.INVOKEINTERFACE, injectionType.getInternalName(), "getDelegate", "()Ljava/util/function/Predicate;", true);

                        // return Predicate#test(item)
                        canEnchant.visitVarInsn(Opcodes.ALOAD, 1);
                        canEnchant.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/Predicate", "test", "(Ljava/lang/Object;)Z", true);
                        canEnchant.visitInsn(Opcodes.IRETURN);

                        canEnchant.visitLabel(label1);
                        canEnchant.visitLocalVariable("this", "L" + classNode.name + ";", null, label0, label1, 0);
                        canEnchant.visitLocalVariable("item", itemType.getDescriptor(), null, label0, label1, 1);

                        canEnchant.visitMaxs(0, 0);
                        canEnchant.visitEnd();
                    }

                    {
                        // more workarounds. this is to be able to create a new category
                        // without calling <init> directly.

                        // EnchantmentCategory.kilt$createCategory(String, int)
                        var initializerAccessor = classNode.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, "kilt$createCategory", "(Ljava/lang/String;I)" + categoryType.getDescriptor(), null, null);
                        initializerAccessor.visitCode();

                        initializerAccessor.visitTypeInsn(Opcodes.NEW, classNode.name);
                        initializerAccessor.visitInsn(Opcodes.DUP);

                        initializerAccessor.visitVarInsn(Opcodes.ALOAD, 0);
                        initializerAccessor.visitVarInsn(Opcodes.ILOAD, 1);
                        initializerAccessor.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.name, "<init>", "(Ljava/lang/String;I)V", false);
                        initializerAccessor.visitInsn(Opcodes.ARETURN);

                        initializerAccessor.visitMaxs(0, 0);
                        initializerAccessor.visitEnd();
                    }

                    var classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(classWriter);

                    var byteArray = classWriter.toByteArray();

                    // export the generated classes so i know what's happening
                    if (System.getProperty("mixin.debug.export").equalsIgnoreCase("true")) {
                        var file = new File(FabricLoader.getInstance().getGameDir().toFile(), ".mixin.out/class/" + classNode.name + ".class");
                        try {
                            file.createNewFile();
                            Files.write(byteArray, file);

                            Kilt.Companion.getLogger().info("Dumped class file for " + classNode.name);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    var clazz = EnumUtils.loadClass(classNode.name, byteArray);

                    if (clazz == null)
                        throw new IllegalStateException();

                    var methods = clazz.getMethods();
                    var declaredMethods = clazz.getDeclaredMethods();

                    try {
                        return (EnchantmentCategory) clazz.getDeclaredMethod("kilt$createCategory", String.class, int.class).invoke(null, name, size);
                    } catch (IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                },
                (values) -> EnchantmentCategoryAccessor.setValues(values.toArray(new EnchantmentCategory[0]))
        );

        ((EnchantmentCategoryInjection) (Object) value).setDelegate(delegate);

        return value;
    }

    default Predicate<Item> getDelegate() {
        throw new IllegalStateException();
    }

    default void setDelegate(Predicate<Item> delegate) {
        throw new IllegalStateException();
    }
}
