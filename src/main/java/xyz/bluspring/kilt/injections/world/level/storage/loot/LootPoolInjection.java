package xyz.bluspring.kilt.injections.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import xyz.bluspring.kilt.util.KiltHelper;

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
}
