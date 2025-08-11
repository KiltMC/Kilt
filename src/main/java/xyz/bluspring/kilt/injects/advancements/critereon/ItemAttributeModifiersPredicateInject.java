package xyz.bluspring.kilt.injects.advancements.critereon;

import net.minecraft.advancements.critereon.ItemAttributeModifiersPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemAttributeModifiersPredicate.class)
public abstract class ItemAttributeModifiersPredicateInject implements SingleComponentItemPredicate<ItemAttributeModifiers> {
    @Unique
    @Override
    public boolean matches(ItemStack stack) {
        return matches(stack, stack.getAttributeModifiers());
    }
}
