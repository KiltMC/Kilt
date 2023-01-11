package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Player.BedSleepingProblem
import java.util.Optional

class PlayerSleepInBedEvent(player: Player, pos: Optional<BlockPos>) : PlayerEvent(player) {
    val optionalPos = pos
    val pos: BlockPos? = pos.orElse(null)

    var result: BedSleepingProblem? = null

    val resultStatus: BedSleepingProblem?
        get() = result
}