package xyz.bluspring.kilt.forgeinjects.world.item.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentInjection;

@Mixin(Enchantment.class)
public class EnchantmentInject implements EnchantmentInjection, IForgeEnchantment {
    @Inject(at = @At("HEAD"), method = "canEnchant", cancellable = true)
    public void kilt$useEnchantingTableCheck(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(canApplyAtEnchantingTable(itemStack));
    }
}
