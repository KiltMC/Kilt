package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class PlayerSetSpawnEvent(player: Player, val spawnLevel: ResourceKey<Level>, val newSpawn: BlockPos?, forced: Boolean) : PlayerEvent(player) {
    val isForced = forced
}