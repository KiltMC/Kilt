package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

@Mixin(LootContext.Builder.class)
public interface LootContextBuilderAccessor {
    @Accessor
    RandomSource getRandom();

    @Accessor
    LootParams getParams();
}
