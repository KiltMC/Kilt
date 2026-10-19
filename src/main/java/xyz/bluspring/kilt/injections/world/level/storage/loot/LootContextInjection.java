package xyz.bluspring.kilt.injections.world.level.storage.loot;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;

public interface LootContextInjection {
    default void setQueriedLootTableId(@Nullable Identifier queriedLootTableId) {
        throw KiltHelper.createMixinException(LootContextInjection.class, "setQueriedLootTableId");
    }

    default Identifier getQueriedLootTableId() {
        throw KiltHelper.createMixinException(LootContextInjection.class, "getQueriedLootTableId");
    }

    default Identifier kilt$getQueriedLootTableId() {
        throw KiltHelper.createMixinException(BuilderInjection.class, "kilt$getQueriedLootTableId");
    }

    interface BuilderInjection {
        default LootContext.Builder withQueriedLootTableId(Identifier queriedLootTableId) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "withQueriedLootTableId");
        }

        default String getName() {
            throw KiltHelper.createMixinException(BuilderInjection.class, "getName");
        }
    }
}
