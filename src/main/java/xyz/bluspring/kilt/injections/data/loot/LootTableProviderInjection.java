package xyz.bluspring.kilt.injections.data.loot;

import java.util.List;
import java.util.Map;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;

@FabricInjectedInterface(LootTableProvider.class)
public interface LootTableProviderInjection {
    default List<LootTableProvider.SubProviderEntry> getTables() {
        throw new IllegalStateException();
    }

    default void validate(Map<Identifier, LootTable> map, ValidationContext validationContext) {
        throw new IllegalStateException();
    }
}
