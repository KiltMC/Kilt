package xyz.bluspring.kilt.injects.world.level.storage.loot;

import io.github.fabricators_of_create.porting_lib.loot.extensions.LootPoolExtensions;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootPoolInjection;

import java.util.Optional;

@Mixin(LootPool.class)
public abstract class LootPoolInject implements LootPoolInjection, LootPoolExtensions {
    @Shadow @Final @Mutable public NumberProvider rolls;
    @Shadow @Final @Mutable public NumberProvider bonusRolls;

    LootPoolInject(LootPoolEntryContainer[] entries, LootItemCondition[] conditions, LootItemFunction[] functions, NumberProvider rolls, NumberProvider bonusRolls) {}

    @CreateInitializer
    LootPoolInject(LootPoolEntryContainer[] entries, LootItemCondition[] conditions, LootItemFunction[] functions, NumberProvider rolls, NumberProvider bonusRolls, Optional<String> name) {
        this(entries, conditions, functions, rolls, bonusRolls);
        this.setName(name.orElse(null));
    }

    @Unique private boolean isFrozen = false;

    @Override
    public void freeze() {
        this.isFrozen = true;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    private void checkFrozen() {
        if (this.isFrozen())
            throw new RuntimeException("Attempted to modify LootPool after being frozen!");
    }

    @Override
    public NumberProvider getRolls() {
        return this.rolls;
    }

    @Override
    public NumberProvider getBonusRolls() {
        return bonusRolls;
    }

    @Override
    public void setRolls(NumberProvider rolls) {
        checkFrozen();
        this.rolls = rolls;
    }

    @Override
    public void setBonusRolls(NumberProvider provider) {
        checkFrozen();
        this.bonusRolls = provider;
    }
}
