package xyz.bluspring.kilt.injects.core.component.predicates;

import java.util.List;

import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;

@Mixin(EnchantmentsPredicate.class)
public abstract class EnchantmentsPredicateInject {
    @Mixin(EnchantmentsPredicate.Enchantments.class)
    public abstract static class EnchantmentsInject extends EnchantmentsPredicate {
        protected EnchantmentsInject(List<EnchantmentPredicate> enchantments) {
            super(enchantments);
        }

        @Override
        public boolean matches(DataComponentGetter components) {
            if (components instanceof ItemStack stack) {
                var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
                if (lookup != null) {
                    return this.matches(stack.getAllEnchantments(lookup));
                }
            }

            return super.matches(components);
        }
    }
}
