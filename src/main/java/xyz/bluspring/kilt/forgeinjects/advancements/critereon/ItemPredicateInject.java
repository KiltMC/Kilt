package xyz.bluspring.kilt.forgeinjects.advancements.critereon;

import net.minecraft.advancements.critereon.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.advancements.critereon.ItemPredicateInjection;

@Mixin(ItemPredicate.class)
public class ItemPredicateInject implements ItemPredicateInjection {
}
