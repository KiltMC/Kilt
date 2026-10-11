package xyz.bluspring.kilt.injects.core.component.predicates;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.AttributeModifiersPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(AttributeModifiersPredicate.class)
public abstract class AttributeModifiersPredicateInject implements SingleComponentItemPredicate<ItemAttributeModifiers> {
    @Override
    public boolean matches(DataComponentGetter components) {
        if (components instanceof ItemStack stack)
            return this.matches(stack.getAttributeModifiers());

        return SingleComponentItemPredicate.super.matches(components);
    }
}
