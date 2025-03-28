package xyz.bluspring.kilt.injections.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public interface LootPoolInjection {
    void freeze();
    boolean isFrozen();
    NumberProvider getRolls();
    NumberProvider getBonusRolls();
    void setRolls(NumberProvider provider);
    void setBonusRolls(NumberProvider provider);
}
