package xyz.bluspring.kilt.injects.server.commands;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(EnchantCommand.class)
public abstract class EnchantCommandInject {
    @WrapOperation(method = "enchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean kilt$checkStackSupportsEnchant(Enchantment instance, ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) Holder<Enchantment> enchantmentHolder) {
        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "supportsEnchantment", ItemStack.class, Holder.class)) {
            return stack.supportsEnchantment(enchantmentHolder);
        } else {
            return original.call(instance, stack);
        }
    }
}
