package net.minecraftforge.event

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTables
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

@Cancelable
class LootTableLoadEvent(val name: ResourceLocation, var table: LootTable, val lootTableManager: LootTables) : Event() {
}