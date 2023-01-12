package net.minecraftforge.event.level

import net.minecraft.server.level.ChunkHolder
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.eventbus.api.Event

class ChunkTicketLevelUpdatedEvent(val level: ServerLevel, val chunkPos: Long, val oldTicketLevel: Int, val newTicketLevel: Int, val chunkHolder: ChunkHolder?) : Event() {
}