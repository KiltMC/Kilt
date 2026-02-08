package xyz.bluspring.kilt.injections.world.level.storage.loot;

import io.github.fabricators_of_create.porting_lib.loot.extensions.LootTableExtensions;
import net.minecraft.world.level.storage.loot.LootPool;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

public interface LootTableInjection extends LootTableExtensions {
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
