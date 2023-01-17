package xyz.bluspring.kilt.forgeinjects.data.loot;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.LootTableProviderInjection;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(LootTableProvider.class)
public class LootTableProviderInject implements LootTableProviderInjection {
    @Shadow @Final private List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders;

    @Override
    public List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return this.subProviders;
    }

    @Override
    public void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
        for (ResourceLocation location : Sets.difference(BuiltInLootTables.all(), map.keySet())) {
            validationContext.reportProblem("Missing built-in table: " + location);
        }

        map.forEach((location, table) -> {
            LootTables.validate(validationContext, location, table);
        });
    }
}
