package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class KiltEarlyRiser : Runnable {
    override fun run() {
        // EnchantmentCategory has an abstract method that doesn't exactly play nicely with enum extension.
        // So, we need to modify it to have a method body that works with the Forge API.
        run {
            val namespace = FabricLauncherBase.getLauncher().targetNamespace

            // The remapper wouldn't fucking remap, might as well do it manually - Kilt isn't supposed to run
            // in other mod devs' environments anyway.
            val enchantmentCategory = if (namespace == "intermediary") "net.minecraft.class_1886" else "net.minecraft.world.item.enchantment.EnchantmentCategory"

            ClassTinkerers.addTransformation(enchantmentCategory) { classNode ->
                val classVisitor = object : ClassVisitor(Opcodes.ASM9) {
                    override fun visitMethod(
                        access: Int,
                        name: String?,
                        descriptor: String?,
                        signature: String?,
                        exceptions: Array<out String>?
                    ): MethodVisitor? {
                        // Delete the original canEnchant
                        if (name == (if (namespace == "intermediary") "method_8177" else "canEnchant"))
                            return AddCanEnchantMethodBody(namespace, enchantmentCategory)

                        return super.visitMethod(access, name, descriptor, signature, exceptions)
                    }
                }

                classNode.accept(classVisitor)
            }
        }
    }

    class AddCanEnchantMethodBody(namespace: String, private val enchantmentCategory: String) : MethodVisitor(Opcodes.ASM9) {
        // This method should become:
        /*
        public boolean canEnchant(Item item) {
            return ((EnchantmentCategoryInjection) this).getPredicate() != null && ((EnchantmentCategoryInjection) this).getPredicate().test(item);
        }
         */

        private val item = if (namespace == "intermediary") "net.minecraft.class_1792" else "net.minecraft.world.item.Item"
        private val enchCategoryInjection = "xyz/bluspring/kilt/injections/world/item/enchantment/EnchantmentCategoryInjection"

        override fun visitCode() {
            super.visitCode()

            val canEnchant = this

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