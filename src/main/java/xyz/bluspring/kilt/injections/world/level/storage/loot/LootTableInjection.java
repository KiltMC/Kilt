package xyz.bluspring.kilt.injections.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.LootPool;

public interface LootTableInjection {
    default void freeze() {
        throw new IllegalStateException();
    }
    boolean isFrozen();
    LootPool getPool(String name);
    LootPool removePool(String name);
    void addPool(LootPool pool);
}
