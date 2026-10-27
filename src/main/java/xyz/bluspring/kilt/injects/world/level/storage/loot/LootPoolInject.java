package xyz.bluspring.kilt.injects.world.level.storage.loot;

import java.util.List;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootPoolInjection;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(LootPool.class)
public abstract class LootPoolInject implements LootPoolInjection {
    @Shadow private LootPoolInject(final List<LootPoolEntryContainer> entries, final List<LootItemCondition> conditions, final List<LootItemFunction> functions, final NumberProvider rolls, final NumberProvider bonusRolls) {}
    @Shadow public NumberProvider rolls;
    @Shadow public NumberProvider bonusRolls;

    @CreateInitializer
    private LootPoolInject(final List<LootPoolEntryContainer> entries, final List<LootItemCondition> conditions, final List<LootItemFunction> functions, final NumberProvider rolls, final NumberProvider bonusRolls, Optional<String> name) {
        this(entries, conditions, functions, rolls, bonusRolls);
    }

    @Unique private boolean isFrozen = false;

    @Override
    public void freeze() {
        this.isFrozen = true;
    }

    @Override
    public boolean isFrozen() {
        return this.isFrozen;
    }

    @Unique
    private void checkFrozen() {
        if (this.isFrozen())
            throw new RuntimeException("Attempted to modify LootPool after being frozen!");
    }

    @Nullable private String name;

    @Override
    public @Nullable String getName() {
        return this.name;
    }

    @Override
    public void setName(@Nullable String name) {
        if (this.name != null) {
            throw new UnsupportedOperationException("Cannot change the name of a pool when it has a name set!");
        }

        this.name = name;
    }

    @Accessor
    @Override
    public abstract NumberProvider getRolls();

    @Accessor
    @Override
    public abstract NumberProvider getBonusRolls();

    @Override
    public void setRolls(NumberProvider rolls) {
        checkFrozen();
        this.rolls = rolls;
    }

    @Override
    public void setBonusRolls(NumberProvider bonusRolls) {
        checkFrozen();
        this.bonusRolls = bonusRolls;
    }

    @Mixin(LootPool.Builder.class)
    public abstract static class BuilderInject implements BuilderInjection {
        @Nullable private String name;

        @Override
        public LootPool.Builder name(String name) {
            this.name = name;
            return (LootPool.Builder) (Object) this;
        }

        @ModifyReturnValue(method = "build", at = @At("RETURN"))
        private LootPool kilt$assignNameToPool(LootPool original) {
            original.setName(this.name);
            return original;
        }
    }
}
