package xyz.bluspring.kilt.injections.world.level.storage.loot;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.storage.loot.LootPool;

public interface LootTableInjection {
    default void freeze() {
        throw KiltHelper.createMixinException(LootTableInjection.class, "freeze");
    }

    default boolean isFrozen() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "isFrozen");
    }

    default LootPool getPool(String name)  {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "getPool");
    }

    default LootPool removePool(String name) {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "removePool");
    }

    default void addPool(LootPool pool) {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "addPool");
    }
}
