package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootTableInjection;

@Mixin(LootTable.class)
public abstract class LootTableInject implements LootTableInjection {
    @Shadow @Final public LootPool[] pools;
    @Unique private boolean isFrozen = false;

    @Override
    public void freeze() {
        this.isFrozen = true;
        //this.pools.forEach(LootPool::freeze);
    }
}
