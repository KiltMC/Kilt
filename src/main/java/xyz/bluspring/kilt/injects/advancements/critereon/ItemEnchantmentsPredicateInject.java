package xyz.bluspring.kilt.injects.advancements.critereon;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ItemEnchantmentsPredicate.class)
public abstract class ItemEnchantmentsPredicateInject {
    @Mixin(ItemEnchantmentsPredicate.Enchantments.class)
    public abstract static class EnchantmentsInject extends ItemEnchantmentsPredicate {
        protected EnchantmentsInject(List<EnchantmentPredicate> enchantments) {
            super(enchantments);
        }

        @Unique
        @Override
        public boolean matches(ItemStack stack) {
            var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
            if (lookup != null)
                return matches(stack, stack.getAllEnchantments(lookup));

            return super.matches(stack);
        }
    }
}
