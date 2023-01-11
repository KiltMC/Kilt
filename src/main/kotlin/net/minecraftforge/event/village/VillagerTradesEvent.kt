package net.minecraftforge.event.village

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing
import net.minecraftforge.eventbus.api.Event

class VillagerTradesEvent(val trades: Int2ObjectMap<List<ItemListing>>, val type: VillagerProfession) : Event() {
}