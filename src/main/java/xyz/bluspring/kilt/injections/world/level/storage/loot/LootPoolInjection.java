package xyz.bluspring.kilt.injections.world.level.storage.loot;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public interface LootPoolInjection {
    default void freeze() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "freeze");
    }

    default boolean isFrozen() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "isFrozen");
    }

    default NumberProvider getRolls() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "getRolls");
    }

    default NumberProvider getBonusRolls() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "getBonusRolls");
    }

    default void setRolls(NumberProvider provider) {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "setRolls");
    }

    default void setBonusRolls(NumberProvider provider) {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "setBonusRolls");
    }

    @Nullable
    default String getName() {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "getName");
    }

    default void setName(String name) {
        throw KiltHelper.createMixinException(LootPoolInjection.class, "setName");
    }
}
