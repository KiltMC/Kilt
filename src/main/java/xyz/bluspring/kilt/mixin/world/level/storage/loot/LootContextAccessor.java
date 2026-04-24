package xyz.bluspring.kilt.mixin.world.level.storage.loot;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

@Mixin(LootContext.class)
public interface LootContextAccessor {
    @Accessor
    LootParams getParams();

    @Accessor
    RandomSource getRandom();
}
