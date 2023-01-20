/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import xyz.bluspring.kilt.injections.LootTableProviderInjection;
import xyz.bluspring.kilt.mixin.*;

/**
 * Currently used only for replacing shears item to shears_dig tool action
 */
public final class ForgeLootTableProvider extends LootTableProvider implements LootTableProviderInjection {

    public ForgeLootTableProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    public void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        // do not validate against all registered loot tables
    }

    @Override
    public List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return super.getTables().stream().map(pair -> {
            // provides new consumer with filtering only changed loot tables and replacing condition item to condition tag
            return new Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>(() -> replaceAndFilterChangesOnly(pair.getFirst().get()), pair.getSecond());
        }).collect(Collectors.toList());
    }

    private Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> replaceAndFilterChangesOnly(Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> consumer) {
        return (newConsumer) -> consumer.accept((resourceLocation, builder) -> {
            if (findAndReplaceInLootTableBuilder(builder, Items.SHEARS, ToolActions.SHEARS_DIG)) {
                newConsumer.accept(resourceLocation, builder);
            }
        });
    }

    private boolean findAndReplaceInLootTableBuilder(LootTable.Builder builder, Item from, ToolAction toolAction) {
        List<LootPool> lootPools = ((BuilderAccessor) builder).getPools();
        boolean found = false;

        for (LootPool lootPool : lootPools) {
            if (findAndReplaceInLootPool(lootPool, from, toolAction)) {
                found = true;
            }
        }

        return found;
    }

    private boolean findAndReplaceInLootPool(LootPool lootPool, Item from, ToolAction toolAction) {
        LootPoolEntryContainer[] lootEntries = lootPool.entries;
        LootItemCondition[] lootConditions = lootPool.conditions;
        boolean found = false;

        for (LootPoolEntryContainer lootEntry : lootEntries) {
            if (findAndReplaceInLootEntry(lootEntry, from, toolAction)) {
                found = true;
            }
            if (lootEntry instanceof CompositeEntryBase) {
                if (findAndReplaceInParentedLootEntry((CompositeEntryBase) lootEntry, from, toolAction)) {
                    found = true;
                }
            }
        }

        for (int i = 0; i < lootConditions.length; i++) {
            LootItemCondition lootCondition = lootConditions[i];
            if (lootCondition instanceof MatchTool && checkMatchTool((MatchTool) lootCondition, from)) {
                lootConditions[i] = CanToolPerformAction.canToolPerformAction(toolAction).build();
                found = true;
            } else if (lootCondition instanceof InvertedLootItemCondition) {
                LootItemCondition invLootCondition = ((InvertedLootItemConditionAccessor) lootCondition).getTerm();

                if (invLootCondition instanceof MatchTool && checkMatchTool((MatchTool) invLootCondition, from)) {
                    lootConditions[i] = InvertedLootItemCondition.invert(CanToolPerformAction.canToolPerformAction(toolAction)).build();
                    found = true;
                } else if (invLootCondition instanceof AlternativeLootItemCondition && findAndReplaceInAlternative((AlternativeLootItemCondition) invLootCondition, from, toolAction)) {
                    found = true;
                }
            }
        }

        return found;
    }

    private boolean findAndReplaceInParentedLootEntry(CompositeEntryBase entry, Item from, ToolAction toolAction) {
        LootPoolEntryContainer[] lootEntries = ((CompositeEntryBaseAccessor) entry).getChildren();
        boolean found = false;

        for (LootPoolEntryContainer lootEntry : lootEntries) {
            if (findAndReplaceInLootEntry(lootEntry, from, toolAction)) {
                found = true;
            }
        }

        return found;
    }

    private boolean findAndReplaceInLootEntry(LootPoolEntryContainer entry, Item from, ToolAction toolAction) {
        LootItemCondition[] lootConditions = ((LootPoolEntryContainerAccessor) entry).getConditions();
        boolean found = false;

        for (int i = 0; i < lootConditions.length; i++) {
            if (lootConditions[i] instanceof AlternativeLootItemCondition && findAndReplaceInAlternative((AlternativeLootItemCondition) lootConditions[i], from, toolAction)) {
                found = true;
            } else if (lootConditions[i] instanceof MatchTool && checkMatchTool((MatchTool) lootConditions[i], from)) {
                lootConditions[i] = CanToolPerformAction.canToolPerformAction(toolAction).build();
                found = true;
            }
        }

        return found;
    }

    private boolean findAndReplaceInAlternative(AlternativeLootItemCondition alternative, Item from, ToolAction toolAction) {
        LootItemCondition[] lootConditions = ((AlternativeLootItemConditionAccessor) alternative).getTerms();
        boolean found = false;

        for (int i = 0; i < lootConditions.length; i++) {
            if (lootConditions[i] instanceof MatchTool && checkMatchTool((MatchTool) lootConditions[i], from)) {
                lootConditions[i] = CanToolPerformAction.canToolPerformAction(toolAction).build();
                found = true;
            }
        }

        return found;
    }

    private boolean checkMatchTool(MatchTool lootCondition, Item expected) {
        ItemPredicate predicate = ((MatchToolAccessor) lootCondition).getPredicate();
        Set<Item> items = ((ItemPredicateAccessor) predicate).getItems();
        return items != null && items.contains(expected);
    }
}
