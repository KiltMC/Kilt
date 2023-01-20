package xyz.bluspring.kilt.mixin;

import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AlternativeLootItemCondition.class)
public interface AlternativeLootItemConditionAccessor {
    @Accessor
    LootItemCondition[] getTerms();
}
