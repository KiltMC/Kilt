package xyz.bluspring.kilt.injections;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;

import java.util.List;
import java.util.Map;

public interface LootTableProviderInjection {
    default List<LootTableProvider.SubProviderEntry> getTables() {
        throw new IllegalStateException();
    }

    default void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
        throw new IllegalStateException();
    }
}
