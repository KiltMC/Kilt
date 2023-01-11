package net.minecraftforge.event.brewing

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.event.entity.player.PlayerEvent

class PlayerBrewedPotionEvent(player: Player, val stack: ItemStack) : PlayerEvent(player) {
}