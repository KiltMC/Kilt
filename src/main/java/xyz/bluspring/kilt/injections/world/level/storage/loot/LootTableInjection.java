package xyz.bluspring.kilt.injections.world.level.storage.loot;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootTableInjection {
    Codec<Optional<WithConditions<LootTable>>> CONDITIONAL_DIRECT_CODEC = ConditionalOps.createConditionalCodecWithConditions(LootTable.DIRECT_CODEC);

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

    interface BuilderInjection {
        default LootTable.Builder withCondition(ICondition condition) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "withCondition");
        }

        default LootTable.Builder withConditions(List<ICondition> condition) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "withConditions");
        }

        default List<ICondition> buildConditions() {
            throw KiltHelper.createMixinException(BuilderInjection.class, "buildConditions");
        }
    }
}
