package net.minecraftforge.event.level

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraftforge.eventbus.api.Event

open class ChunkWatchEvent(val player: ServerPlayer, val pos: ChunkPos, val level: ServerLevel) : Event() {
    class Watch(player: ServerPlayer, val chunk: LevelChunk, level: ServerLevel) : ChunkWatchEvent(player, chunk.pos, level)

    class UnWatch(player: ServerPlayer, pos: ChunkPos, level: ServerLevel) : ChunkWatchEvent(player, pos, level)
}