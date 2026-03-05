package xyz.bluspring.kilt.injects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(EnchantRandomlyFunction.class)
public abstract class EnchantRandomlyFunctionInject {
    @WrapOperation(method = "method_60291", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean kilt$tryCheckSupportsEnchantment(Enchantment instance, ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) Holder<Enchantment> holder) {
        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "supportsEnchantment", ItemStack.class, Holder.class)) {
            return stack.supportsEnchantment(holder);
        }

        return original.call(instance, stack);
    }
}
