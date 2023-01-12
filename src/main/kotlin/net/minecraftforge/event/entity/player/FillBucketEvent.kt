package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.Event.HasResult

@Cancelable
@HasResult
class FillBucketEvent(player: Player, current: ItemStack, val level: Level, val target: HitResult?) : PlayerEvent(player) {
    val emptyBucket = current
    var filledBucket: ItemStack? = null
}