package xyz.bluspring.kilt.injectors

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

class KiltEarlyRiser : Runnable {
    override fun run() {
        val remapper = FabricLoader.getInstance().mappingResolver
        val namespace = FabricLauncherBase.getLauncher().targetNamespace

        // EnchantmentHelper
        ClassTinkerers.addTransformation(remapper.mapClassName("intermediary", "net.minecraft.class_1890")) { classNode ->
            // apparently, Forge renames getItemEnchantmentLevel to getTagEnchantmentLevel for whatever reason.
            // why? don't fucking know.
            // getItemEnchantmentLevel(Enchantment, ItemStack)I
            val gielDescriptor = "(L${remapper.mapClassName("intermediary", "net.minecraft.class_1887")};L${remapper.mapClassName("intermediary", "net.minecraft.class_1799")};)I"

            val getItemEnchantmentLevel = classNode.visitMethod(
                0x9,
                remapper.mapMethodName("intermediary", classNode.name, "method_8225", "(Lnet/minecraft/class_1887;Lnet/minecraft/class_1799;)I"),
                gielDescriptor, null, arrayOf())

            // free will was a mistake
            classNode.methods.add(MethodNode(Opcodes.ASM9, "getTagEnchantmentLevel", gielDescriptor, null, arrayOf()).apply {
                this.accept(getItemEnchantmentLevel)
            })
        }
    }
}