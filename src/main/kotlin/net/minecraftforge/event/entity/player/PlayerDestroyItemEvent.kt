package net.minecraftforge.event.entity.player

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class PlayerDestroyItemEvent(player: Player, val original: ItemStack, val hand: InteractionHand?) : PlayerEvent(player) {
}