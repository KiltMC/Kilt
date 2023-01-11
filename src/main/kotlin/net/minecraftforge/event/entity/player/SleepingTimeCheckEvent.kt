package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraftforge.eventbus.api.Event.HasResult
import java.util.*

@HasResult
class SleepingTimeCheckEvent(player: Player, val sleepingLocation: Optional<BlockPos>) : PlayerEvent(player) {
}