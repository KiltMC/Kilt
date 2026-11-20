package xyz.bluspring.kilt.injects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

@Mixin(EnchantRandomlyFunction.class)
public abstract class EnchantRandomlyFunctionInject {
    @WrapOperation(method = "lambda$run$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean kilt$tryCheckSupportsEnchantment(Enchantment instance, ItemStack itemStack, Operation<Boolean> original, @Local(argsOnly = true, name = "candidate") Holder<Enchantment> holder) {
        if (KiltHelper.INSTANCE.hasMethodOverride(itemStack.getItem().getClass(), IItemExtension.class, "supportsEnchantment", ItemStack.class, Holder.class)) {
            return itemStack.supportsEnchantment(holder);
        }

        return original.call(instance, itemStack);
    }
}
