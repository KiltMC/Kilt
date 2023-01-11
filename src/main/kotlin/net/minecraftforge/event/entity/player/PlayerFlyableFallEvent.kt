package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.player.Player

class PlayerFlyableFallEvent(player: Player, var distance: Float, var multiplier: Float) : PlayerEvent(player) {
}