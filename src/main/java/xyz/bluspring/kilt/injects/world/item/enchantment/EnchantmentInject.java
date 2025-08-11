// TRACKED HASH: 6102b0cff7eff62c7f1b633d6817a149258e975d
package xyz.bluspring.kilt.injects.world.item.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentInjection;

@Mixin(Enchantment.class)
public class EnchantmentInject implements EnchantmentInjection, IForgeEnchantment {
    @ModifyReturnValue(at = @At("RETURN"), method = "canEnchant")
    public boolean kilt$useEnchantingTableCheck(boolean original, ItemStack stack) {
        return original || canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.canApplyAtEnchantingTable((Enchantment) (Object) this);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}