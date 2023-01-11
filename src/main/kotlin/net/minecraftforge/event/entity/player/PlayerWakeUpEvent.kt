package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.player.Player

class PlayerWakeUpEvent(player: Player, val wakeImmediately: Boolean, val updateLevel: Boolean) : PlayerEvent(player) {
}